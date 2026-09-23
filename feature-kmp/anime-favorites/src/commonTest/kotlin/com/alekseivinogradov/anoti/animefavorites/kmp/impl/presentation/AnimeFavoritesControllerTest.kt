package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.usecase.fake.UpdateAllAnimeInBackgroundOnceUsecaseFake
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.AnimeDatabaseExecutorImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.AnimeDatabaseStoreFactory
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.fake.AnimeDatabaseUsecasesFake
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.AnimeFavoritesView
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.AnimeFavoritesUiModel
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.store.AnimeFavoritesExecutorFactory
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.store.AnimeFavoritesExecutorImpl
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.store.AnimeFavoritesMainStoreFactory
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase.wrapper.FavoritesUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderBase
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.fake.CoroutineContextProviderFake
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
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
class AnimeFavoritesControllerTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val createdStores = mutableListOf<Store<*, *, *>>()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        createdStores.forEach { it.dispose() }
        Dispatchers.resetMain()
    }

    private class AnimeFavoritesViewFake :
        BaseMviView<AnimeFavoritesUiModel, AnimeFavoritesMainStore.Intent>(),
        AnimeFavoritesView {

        val renderedModels = mutableListOf<AnimeFavoritesUiModel>()

        override val renderer: ViewRenderer<AnimeFavoritesUiModel> =
            object : ViewRenderer<AnimeFavoritesUiModel> {
                override fun render(model: AnimeFavoritesUiModel) {
                    renderedModels += model
                }
            }
    }

    private object NoOpFavoritesSourceFake : AnimeFavoritesSource {
        override suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain> {
            error("not used in AnimeFavoritesControllerTest")
        }
    }

    /** The saved-anime database every [AnimeDatabaseStore] usecase reads from and writes to. */
    /** Everything a test needs to drive one controller and see where its bindings lead. */
    private class Wiring(
        val lifecycle: LifecycleRegistry,
        val view: AnimeFavoritesViewFake,
        val mainStore: AnimeFavoritesMainStore,
        val animeDatabaseStore: AnimeDatabaseStore,
        val database: AnimeDatabaseUsecasesFake,
        val backgroundUpdateUsecase: UpdateAllAnimeInBackgroundOnceUsecaseFake
    )

    private fun testDbItem(id: AnimeId, name: String = "Item $id"): AnimeDbDomain {
        return AnimeDbDomain(
            id = id,
            imageUrl = null,
            name = name,
            episodesAired = 1,
            episodesTotal = 12,
            nextEpisodeAt = null,
            airedOn = null,
            releasedOn = null,
            score = 8.0F,
            releaseStatus = ReleaseStatusDb.ONGOING,
            episodesViewed = 0,
            isNewEpisode = false
        )
    }

    private fun createCoroutineContextProvider(): CoroutineContextProviderBase {
        return CoroutineContextProviderFake()
    }

    private fun createAnimeDatabaseStore(
        database: AnimeDatabaseUsecasesFake,
        coroutineContextProvider: CoroutineContextProviderBase
    ): AnimeDatabaseStore {
        return AnimeDatabaseStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = {
                AnimeDatabaseExecutorImpl(
                    coroutineContextProvider = coroutineContextProvider,
                    usecases = database.usecases
                )
            }
        ).create().also(createdStores::add)
    }

    private fun createMainStore(
        backgroundUpdateUsecase: UpdateAllAnimeInBackgroundOnceUsecase,
        coroutineContextProvider: CoroutineContextProviderBase
    ): AnimeFavoritesMainStore {
        val executorFactory: AnimeFavoritesExecutorFactory = {
            AnimeFavoritesExecutorImpl(
                coroutineContextProvider = coroutineContextProvider,
                usecases = FavoritesUsecases(
                    updateAllAnimeInBackgroundOnceUsecase = backgroundUpdateUsecase,
                    fetchAnimeDetailsByIdUsecase = FetchAnimeDetailsByIdUsecase(NoOpFavoritesSourceFake)
                ),
                systemMessageProvider = SystemMessageProvider(
                    makeConnectionErrorSystemMessage = {},
                    makeUnknownErrorSystemMessage = {}
                )
            )
        }
        return AnimeFavoritesMainStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = executorFactory
        ).create().also(createdStores::add)
    }

    private fun createWiring(databaseItems: List<AnimeDbDomain> = emptyList()): Wiring {
        val coroutineContextProvider = createCoroutineContextProvider()
        val database = AnimeDatabaseUsecasesFake(databaseItems)
        val backgroundUpdateUsecase = UpdateAllAnimeInBackgroundOnceUsecaseFake()
        val animeDatabaseStore = createAnimeDatabaseStore(database, coroutineContextProvider)
        val mainStore = createMainStore(backgroundUpdateUsecase, coroutineContextProvider)

        val lifecycle = LifecycleRegistry()
        val view = AnimeFavoritesViewFake()
        AnimeFavoritesController(
            lifecycle = lifecycle,
            mainStore = mainStore,
            animeDatabaseStore = animeDatabaseStore
        ).onViewCreated(mainView = view, viewLifecycle = lifecycle)
        lifecycle.resume()

        return Wiring(
            lifecycle = lifecycle,
            view = view,
            mainStore = mainStore,
            animeDatabaseStore = animeDatabaseStore,
            database = database,
            backgroundUpdateUsecase = backgroundUpdateUsecase
        )
    }

    @Test
    fun aDatabaseStoreStateReachesTheViewAsARenderedUiModel() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring()

        //When
        wiring.database.items.value = listOf(testDbItem(id = 7, name = "Frieren"))
        runCurrent()

        //Then
        val listItems = wiring.view.renderedModels.last().listItems
        assertEquals(1, listItems.size)
        assertEquals(7, listItems.single().id)
        assertEquals("Frieren", listItems.single().name)
    }

    @Test
    fun aViewEventReachesTheMainStore() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring(databaseItems = listOf(testDbItem(id = 7)))

        //When
        wiring.view.dispatch(AnimeFavoritesMainStore.Intent.ItemsSubmittedToList)
        runCurrent()

        //Then
        assertEquals(ContentTypeDomain.LOADED, wiring.mainStore.state.contentType)
    }

    @Test
    fun aMainStoreLabelReachesTheDatabaseStore() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring(databaseItems = listOf(testDbItem(id = 7)))

        //When
        wiring.view.dispatch(AnimeFavoritesMainStore.Intent.NotificationClick(id = 7))
        runCurrent()

        //Then
        assertEquals(listOf(7), wiring.database.deletedIds)
    }

    @Test
    fun aDatabaseStoreLabelReachesTheMainStore() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring(databaseItems = listOf(testDbItem(id = 7)))

        //When
        wiring.view.dispatch(AnimeFavoritesMainStore.Intent.UpdateSection)
        runCurrent()

        //Then
        assertEquals(1, wiring.database.resetNewEpisodeStatusCount)
        assertEquals(1, wiring.backgroundUpdateUsecase.executeCount)
    }

    @Test
    fun destroyingTheLifecycleDisposesBothStores() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring()

        //When
        wiring.lifecycle.destroy()

        //Then
        assertTrue(wiring.mainStore.isDisposed, "the favorites store outlived its screen")
        assertTrue(wiring.animeDatabaseStore.isDisposed, "the database store outlived its screen")
    }
}
