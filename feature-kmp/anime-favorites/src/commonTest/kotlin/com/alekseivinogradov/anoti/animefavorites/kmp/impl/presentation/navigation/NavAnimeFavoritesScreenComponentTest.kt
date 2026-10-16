package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.navigation

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.usecase.fake.UpdateAllAnimeInBackgroundOnceUsecaseFake
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.ANIMATION_DURATION_SHORT
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.AnimeDatabaseExecutorImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.AnimeDatabaseStoreFactory
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.fake.AnimeDatabaseUsecasesFake
import com.alekseivinogradov.anoti.animefavorites.kmp.api.di.DiAnimeFavoritesDependencies
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.di.createDiAnimeFavoritesComponent
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.fake.CoroutineContextProviderFake
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.formatter.fake.DateFormatterFake
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NavAnimeFavoritesScreenComponentTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val lifecycles = mutableListOf<LifecycleRegistry>()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        lifecycles.filter { it.state != Lifecycle.State.DESTROYED }.forEach { it.destroy() }
        Dispatchers.resetMain()
    }

    private class DiAnimeFavoritesDependenciesFake(
        override val animeDatabaseStore: AnimeDatabaseStore,
        override val coroutineContextProvider: CoroutineContextProvider
    ) : DiAnimeFavoritesDependencies {
        override val storeFactory: StoreFactory = DefaultStoreFactory()
        override val dateFormatter: DateFormatter = DateFormatterFake()
        override val updateAllAnimeInBackgroundOnceUsecase: UpdateAllAnimeInBackgroundOnceUsecase =
            UpdateAllAnimeInBackgroundOnceUsecaseFake()
    }

    /** One component, the state keeper it saves through, and what its database reset reaches. */
    private class Wiring(
        val lifecycle: LifecycleRegistry,
        val stateKeeper: StateKeeperDispatcher,
        val component: NavAnimeFavoritesScreenComponent,
        val databaseUsecases: AnimeDatabaseUsecasesFake
    )

    private fun createWiring(
        savedState: SerializableContainer? = null,
        databaseItems: List<AnimeDbDomain> = listOf(),
        isStarted: Boolean = true
    ): Wiring {
        val coroutineContextProvider = CoroutineContextProviderFake()
        val databaseUsecases = AnimeDatabaseUsecasesFake(databaseItems)
        val animeDatabaseStore = AnimeDatabaseStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = {
                AnimeDatabaseExecutorImpl(
                    coroutineContextProvider = coroutineContextProvider,
                    usecases = databaseUsecases.usecases
                )
            }
        ).create()

        val lifecycle = LifecycleRegistry().also(lifecycles::add)
        val stateKeeper = StateKeeperDispatcher(savedState)
        val component = NavAnimeFavoritesScreenComponent(
            componentContext = DefaultComponentContext(
                lifecycle = lifecycle,
                stateKeeper = stateKeeper
            ),
            diAnimeFavoritesComponent = createDiAnimeFavoritesComponent(
                parent = DiAnimeFavoritesDependenciesFake(
                    animeDatabaseStore = animeDatabaseStore,
                    coroutineContextProvider = coroutineContextProvider
                )
            )
        )
        if (isStarted) lifecycle.resume() else lifecycle.create()
        return Wiring(
            lifecycle = lifecycle,
            stateKeeper = stateKeeper,
            component = component,
            databaseUsecases = databaseUsecases
        )
    }

    @Test
    fun startingPutsTheMainStoreIntoItsMinimumDurationLoadingState() = runTest(testDispatcher) {
        //Given
        val savedState: SerializableContainer? = null

        //When
        val wiring = createWiring(savedState = savedState)

        //Then
        assertEquals(
            ContentTypeDomain.LOADING(hasMinimumDuration = true),
            wiring.component.mainStore.state.contentType
        )
    }

    @Test
    fun aFreshArrivalResetsTheDatabaseStoresExtraInfo() = runTest(testDispatcher) {
        //Given
        val savedState: SerializableContainer? = null

        //When
        val wiring = createWiring(savedState = savedState)
        runCurrent()

        //Then
        assertEquals(1, wiring.databaseUsecases.resetExtraInfoCount)
    }

    @Test
    fun aComponentBuiltFromASavedMarkerKeepsTheDatabaseStoresExtraInfo() = runTest(testDispatcher) {
        //Given
        val beforeProcessDeath = createWiring()
        val savedState = beforeProcessDeath.stateKeeper.save()

        //When
        val afterProcessDeath = createWiring(savedState = savedState)
        runCurrent()

        //Then
        assertEquals(
            0,
            afterProcessDeath.databaseUsecases.resetExtraInfoCount,
            "a restored screen must keep the extra info it was showing"
        )
        // The loading treatment is unconditional: only the reset above is skipped on a restore.
        assertEquals(
            ContentTypeDomain.LOADING(hasMinimumDuration = true),
            afterProcessDeath.component.mainStore.state.contentType
        )
    }

    @Test
    fun aFreshArrivalAtAnEmptyDatabaseSettlesOnceTheMinimumLoadingTimeIsOver() =
        runTest(testDispatcher) {
            //Given
            val databaseItems = listOf<AnimeDbDomain>()

            //When
            val wiring = createWiring(databaseItems = databaseItems)
            advanceTimeBy(ANIMATION_DURATION_SHORT)
            runCurrent()

            //Then
            assertEquals(ContentTypeDomain.EMPTY, wiring.component.mainStore.state.contentType)
        }

    @Test
    fun aRestoredArrivalSettlesOnceTheMinimumLoadingTimeIsOver() = runTest(testDispatcher) {
        //Given
        val beforeProcessDeath = createWiring()
        val savedState = beforeProcessDeath.stateKeeper.save()

        //When
        val afterProcessDeath = createWiring(
            savedState = savedState,
            databaseItems = listOf(SAVED_ANIME)
        )
        advanceTimeBy(ANIMATION_DURATION_SHORT)
        runCurrent()

        //Then
        assertEquals(
            ContentTypeDomain.LOADED,
            afterProcessDeath.component.mainStore.state.contentType,
            "the open waited for a list the database had already delivered"
        )
    }

    @Test
    fun theSectionOpensOnceWhateverTheLifecycleDoesNext() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring()
        advanceUntilIdle()
        val settled = wiring.component.mainStore.state.contentType

        //When
        wiring.lifecycle.stop()
        wiring.lifecycle.resume()

        //Then
        assertEquals(settled, wiring.component.mainStore.state.contentType)
        assertEquals(1, wiring.databaseUsecases.resetExtraInfoCount)
    }

    @Test
    fun aComponentThatIsOnlyCreatedOpensNothingUntilItStarts() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring(isStarted = false)

        //When
        advanceUntilIdle()

        //Then
        assertEquals(ContentTypeDomain.LOADING(), wiring.component.mainStore.state.contentType)
        assertEquals(0, wiring.databaseUsecases.resetExtraInfoCount)
    }

    @Test
    fun destroyingTheLifecycleDisposesBothStores() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring()

        //When
        wiring.lifecycle.destroy()

        //Then
        assertTrue(wiring.component.mainStore.isDisposed, "the favorites store outlived its screen")
        assertTrue(
            wiring.component.animeDatabaseStore.isDisposed,
            "the database store outlived its screen"
        )
    }
}

/** A saved anime with only what the favorites list needs to show it. */
private val SAVED_ANIME = AnimeDbDomain(
    id = 7,
    imageUrl = null,
    name = "Frieren",
    episodesAired = null,
    episodesTotal = null,
    nextEpisodeAt = null,
    airedOn = null,
    releasedOn = null,
    score = null,
    releaseStatus = ReleaseStatusDb.ONGOING,
    episodesViewed = 0,
    isNewEpisode = false
)
