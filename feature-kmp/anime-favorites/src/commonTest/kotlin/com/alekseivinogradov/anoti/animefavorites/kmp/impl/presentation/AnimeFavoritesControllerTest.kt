package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.ChangeAnimeDatabaseItemNewEpisodeStatusUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.DeleteAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.FetchAllAnimeDatabaseItemsFlowUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.InsertAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.ResetAllAnimeDatabaseItemsExtraInfoUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.UpdateAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.wrapper.AnimeDatabaseUsecases
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.AnimeDatabaseExecutorImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.AnimeDatabaseStoreFactory
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

    private class FakeAnimeFavoritesView :
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

    private object NoOpFavoritesSource : AnimeFavoritesSource {
        override suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain> {
            error("not used in AnimeFavoritesControllerTest")
        }
    }

    private class RecordingBackgroundUpdateUsecase : UpdateAllAnimeInBackgroundOnceUsecase {
        var executeCount = 0
            private set

        override fun execute() {
            executeCount++
        }
    }

    private class FakeItemsFlowUsecase(
        private val items: Flow<List<AnimeDbDomain>>
    ) : FetchAllAnimeDatabaseItemsFlowUsecase {
        override fun execute(): Flow<List<AnimeDbDomain>> = items
    }

    private class RecordingDeleteUsecase : DeleteAnimeDatabaseItemUsecase {
        val deletedIds = mutableListOf<AnimeId>()

        override suspend fun execute(id: AnimeId) {
            deletedIds += id
        }
    }

    private class RecordingResetNewEpisodeStatusUsecase :
        ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecase {
        var executeCount = 0
            private set

        override suspend fun execute() {
            executeCount++
        }
    }

    private object NoOpInsertUsecase : InsertAnimeDatabaseItemUsecase {
        override suspend fun execute(anime: AnimeDbDomain) = Unit
    }

    private object NoOpChangeNewEpisodeStatusUsecase :
        ChangeAnimeDatabaseItemNewEpisodeStatusUsecase {
        override suspend fun execute(id: Int, isNewEpisode: Boolean) = Unit
    }

    private object NoOpUpdateUsecase : UpdateAnimeDatabaseItemUsecase {
        override suspend fun execute(anime: AnimeDbDomain) = Unit
    }

    private object NoOpResetExtraInfoUsecase : ResetAllAnimeDatabaseItemsExtraInfoUsecase {
        override suspend fun execute() = Unit
    }

    /** The saved-anime database every [AnimeDatabaseStore] usecase reads from and writes to. */
    private class FakeAnimeDatabase(initialItems: List<AnimeDbDomain>) {
        val items = MutableStateFlow(initialItems)
        val deleteUsecase = RecordingDeleteUsecase()
        val resetNewEpisodeStatusUsecase = RecordingResetNewEpisodeStatusUsecase()

        val usecases = AnimeDatabaseUsecases(
            fetchAllAnimeDatabaseItemsFlowUsecase = FakeItemsFlowUsecase(items),
            insertAnimeDatabaseItemUsecase = NoOpInsertUsecase,
            deleteAnimeDatabaseItemUsecase = deleteUsecase,
            resetAllAnimeDatabaseItemsNewEpisodeStatusUsecase = resetNewEpisodeStatusUsecase,
            changeAnimeDatabaseItemNewEpisodeStatusUsecase = NoOpChangeNewEpisodeStatusUsecase,
            updateAnimeDatabaseItemUsecase = NoOpUpdateUsecase,
            resetAllAnimeDatabaseItemsExtraInfoUsecase = NoOpResetExtraInfoUsecase
        )
    }

    /** Everything a test needs to drive one controller and see where its bindings lead. */
    private class Wiring(
        val lifecycle: LifecycleRegistry,
        val view: FakeAnimeFavoritesView,
        val mainStore: AnimeFavoritesMainStore,
        val animeDatabaseStore: AnimeDatabaseStore,
        val database: FakeAnimeDatabase,
        val backgroundUpdateUsecase: RecordingBackgroundUpdateUsecase
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
        return object : CoroutineContextProviderBase() {
            override val exceptionHandlerCallback: (Throwable) -> Unit = {}
        }
    }

    private fun createAnimeDatabaseStore(
        database: FakeAnimeDatabase,
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
                    fetchAnimeDetailsByIdUsecase = FetchAnimeDetailsByIdUsecase(NoOpFavoritesSource)
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
        val database = FakeAnimeDatabase(databaseItems)
        val backgroundUpdateUsecase = RecordingBackgroundUpdateUsecase()
        val animeDatabaseStore = createAnimeDatabaseStore(database, coroutineContextProvider)
        val mainStore = createMainStore(backgroundUpdateUsecase, coroutineContextProvider)

        val lifecycle = LifecycleRegistry()
        val view = FakeAnimeFavoritesView()
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
        assertEquals(listOf(7), wiring.database.deleteUsecase.deletedIds)
    }

    @Test
    fun aDatabaseStoreLabelReachesTheMainStore() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring(databaseItems = listOf(testDbItem(id = 7)))

        //When
        wiring.view.dispatch(AnimeFavoritesMainStore.Intent.UpdateSection)
        runCurrent()

        //Then
        assertEquals(1, wiring.database.resetNewEpisodeStatusUsecase.executeCount)
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
