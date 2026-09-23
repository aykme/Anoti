package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.navigation

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeDetailsResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeShortResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.AnimeDatabaseExecutorImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.AnimeDatabaseStoreFactory
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.fake.AnimeDatabaseUsecasesFake
import com.alekseivinogradov.anoti.animefavorites.kmp.api.di.DiAnimeFavoritesDependencies
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.di.createDiAnimeFavoritesComponent
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderBase
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.formatter.fake.DateFormatterFake
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.alekseivinogradov.anoti.network.kmp.impl.data.fake.SafeApiFake
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

    private object UnreachableApiService : ShikimoriApiService {
        override suspend fun getAnimeList(
            page: Int,
            releaseStatus: String?,
            sort: String?,
            search: String?,
            ids: String?
        ): List<AnimeShortResponse> = error("the favorites screen never lists anime")

        override suspend fun getAnimeById(id: AnimeId): AnimeDetailsResponse {
            error("no anime details are fetched in NavAnimeFavoritesScreenComponentTest")
        }
    }

    private object NoOpBackgroundUpdateUsecase : UpdateAllAnimeInBackgroundOnceUsecase {
        override fun execute() = Unit
    }

    private class FakeDependencies(
        override val animeDatabaseStore: AnimeDatabaseStore,
        override val coroutineContextProvider: CoroutineContextProvider
    ) : DiAnimeFavoritesDependencies {
        override val storeFactory: StoreFactory = DefaultStoreFactory()
        override val systemMessageProvider = SystemMessageProvider(
            makeConnectionErrorSystemMessage = {},
            makeUnknownErrorSystemMessage = {}
        )
        override val dateFormatter: DateFormatter = DateFormatterFake()
        override val shikimoriApiService: ShikimoriApiService = UnreachableApiService
        override val safeApi: SafeApi = SafeApiFake()
        override val updateAllAnimeInBackgroundOnceUsecase: UpdateAllAnimeInBackgroundOnceUsecase =
            NoOpBackgroundUpdateUsecase
    }

    /** One component, the state keeper it saves through, and what its database reset reaches. */
    private class Wiring(
        val lifecycle: LifecycleRegistry,
        val stateKeeper: StateKeeperDispatcher,
        val component: NavAnimeFavoritesScreenComponent,
        val databaseUsecases: AnimeDatabaseUsecasesFake
    )

    private fun createWiring(savedState: SerializableContainer? = null): Wiring {
        val coroutineContextProvider = object : CoroutineContextProviderBase() {
            override val exceptionHandlerCallback: (Throwable) -> Unit = {}
        }
        val databaseUsecases = AnimeDatabaseUsecasesFake()
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
                parent = FakeDependencies(
                    animeDatabaseStore = animeDatabaseStore,
                    coroutineContextProvider = coroutineContextProvider
                )
            )
        )
        lifecycle.resume()
        return Wiring(
            lifecycle = lifecycle,
            stateKeeper = stateKeeper,
            component = component,
            databaseUsecases = databaseUsecases
        )
    }

    @Test
    fun openingTheSectionPutsTheMainStoreIntoItsMinimumDurationLoadingState() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring()

        //When
        wiring.component.openSectionUnlessRestored()

        //Then
        assertEquals(
            ContentTypeDomain.LOADING(hasMinimumDuration = true),
            wiring.component.mainStore.state.contentType
        )
    }

    @Test
    fun aFreshArrivalResetsTheDatabaseStoresExtraInfo() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring()

        //When
        wiring.component.openSectionUnlessRestored()
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
        afterProcessDeath.component.openSectionUnlessRestored()
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
