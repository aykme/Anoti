package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.FIRST_PAGE
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.AnimeDatabaseExecutorImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.AnimeDatabaseStoreFactory
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.fake.AnimeDatabaseUsecasesFake
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.AnimeListView
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.AnimeListUiModel
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.SectionHatUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.NotificationUi
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.announcedsection.AnnouncedSectionExecutorFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.announcedsection.AnnouncedSectionExecutorImpl
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.announcedsection.AnnouncedSectionStoreFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.main.AnimeListExecutorFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.main.AnimeListExecutorImpl
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.main.AnimeListMainStoreFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.ongoingsection.OngoingSectionExecutorFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.ongoingsection.OngoingSectionExecutorImpl
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.ongoingsection.OngoingSectionStoreFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.searchsection.SearchSectionExecutorFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.searchsection.SearchSectionExecutorImpl
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.searchsection.SearchSectionStoreFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnimeListBySearchUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnnouncedAnimeListUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchOngoingAnimeListUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper.AnnouncedUsecases
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper.OngoingUsecases
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper.SearchUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
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
class AnimeListControllerTest {

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

    private class AnimeListViewFake :
        BaseMviView<AnimeListUiModel, AnimeListMainStore.Intent>(),
        AnimeListView {

        val renderedModels = mutableListOf<AnimeListUiModel>()

        override val renderer: ViewRenderer<AnimeListUiModel> =
            object : ViewRenderer<AnimeListUiModel> {
                override fun render(model: AnimeListUiModel) {
                    renderedModels += model
                }
            }
    }

    /** Serves each section its own first page, so a section's items identify their source. */
    private class AnimeListSectionsSourceFake(
        private val ongoingItems: List<ListItemDomain>,
        private val announcedItems: List<ListItemDomain>,
        private val searchItems: List<ListItemDomain>
    ) : AnimeListSource {

        override suspend fun getOngoingList(
            page: Int,
            sort: SortData
        ): CallResult<List<ListItemDomain>> = firstPageOnly(page, ongoingItems)

        override suspend fun getAnnouncedList(
            page: Int,
            sort: SortData
        ): CallResult<List<ListItemDomain>> = firstPageOnly(page, announcedItems)

        override suspend fun getListBySearch(
            page: Int,
            search: String,
            sort: SortData
        ): CallResult<List<ListItemDomain>> = firstPageOnly(page, searchItems)

        override suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain> {
            error("not used in AnimeListControllerTest")
        }

        private fun firstPageOnly(
            page: Int,
            items: List<ListItemDomain>
        ): CallResult<List<ListItemDomain>> {
            return CallResult.Success(if (page == FIRST_PAGE) items else emptyList())
        }
    }

    /** The saved-anime database every [AnimeDatabaseStore] usecase reads from and writes to. */
    /** Everything a test needs to drive one controller and see where its bindings lead. */
    // One parameter per store the controller wires, so the count follows the controller itself.
    @Suppress("LongParameterList")
    private class Wiring(
        val lifecycle: LifecycleRegistry,
        val view: AnimeListViewFake,
        val mainStore: AnimeListMainStore,
        val animeDatabaseStore: AnimeDatabaseStore,
        val ongoingSectionStore: OngoingSectionStore,
        val announcedSectionStore: AnnouncedSectionStore,
        val searchSectionStore: SearchSectionStore,
        val database: AnimeDatabaseUsecasesFake
    )

    private fun testListItem(id: AnimeId, name: String = "Item $id") = ListItemDomain(
        id = id,
        name = name,
        imageUrl = null,
        episodesAired = 1,
        episodesTotal = 12,
        nextEpisodeAt = null,
        airedOn = null,
        releasedOn = null,
        score = 8.0F,
        releaseStatus = ReleaseStatusDomain.ONGOING
    )

    private fun testDbItem(id: AnimeId, name: String = "Item $id") = AnimeDbDomain(
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

    private fun createCoroutineContextProvider() = CoroutineContextProviderFake()

    private fun noOpSystemMessageProvider() = SystemMessageProvider(
        makeConnectionErrorSystemMessage = {},
        makeUnknownErrorSystemMessage = {}
    )

    private fun createWiring(
        ongoingItems: List<ListItemDomain> = listOf(testListItem(id = 1, name = "Frieren")),
        announcedItems: List<ListItemDomain> = listOf(testListItem(id = 2, name = "Dandadan")),
        searchItems: List<ListItemDomain> = listOf(testListItem(id = 3, name = "Bleach")),
        databaseItems: List<AnimeDbDomain> = emptyList()
    ): Wiring {
        val source = AnimeListSectionsSourceFake(ongoingItems, announcedItems, searchItems)
        val database = AnimeDatabaseUsecasesFake(databaseItems)

        val mainStore = createMainStore()
        val animeDatabaseStore = createAnimeDatabaseStore(database)
        val ongoingSectionStore = createOngoingStore(source)
        val announcedSectionStore = createAnnouncedStore(source)
        val searchSectionStore = createSearchStore(source)

        val lifecycle = LifecycleRegistry()
        val view = AnimeListViewFake()
        AnimeListController(
            lifecycle = lifecycle,
            mainStore = mainStore,
            animeDatabaseStore = animeDatabaseStore,
            ongoingSectionStore = ongoingSectionStore,
            announcedSectionStore = announcedSectionStore,
            searchSectionStore = searchSectionStore
        ).onViewCreated(mainView = view, viewLifecycle = lifecycle)
        lifecycle.resume()

        return Wiring(
            lifecycle = lifecycle,
            view = view,
            mainStore = mainStore,
            animeDatabaseStore = animeDatabaseStore,
            ongoingSectionStore = ongoingSectionStore,
            announcedSectionStore = announcedSectionStore,
            searchSectionStore = searchSectionStore,
            database = database
        )
    }

    private fun createMainStore(): AnimeListMainStore {
        val executorFactory: AnimeListExecutorFactory = {
            AnimeListExecutorImpl(coroutineContextProvider = createCoroutineContextProvider())
        }
        return AnimeListMainStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = executorFactory
        ).create().also(createdStores::add)
    }

    private fun createAnimeDatabaseStore(database: AnimeDatabaseUsecasesFake): AnimeDatabaseStore {
        return AnimeDatabaseStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = {
                AnimeDatabaseExecutorImpl(
                    coroutineContextProvider = createCoroutineContextProvider(),
                    usecases = database.usecases
                )
            }
        ).create().also(createdStores::add)
    }

    private fun createOngoingStore(source: AnimeListSource): OngoingSectionStore {
        val executorFactory: OngoingSectionExecutorFactory = {
            OngoingSectionExecutorImpl(
                coroutineContextProvider = createCoroutineContextProvider(),
                usecases = OngoingUsecases(
                    fetchOngoingAnimeListUsecase = FetchOngoingAnimeListUsecase(source),
                    fetchAnimeDetailsByIdUsecase = FetchAnimeDetailsByIdUsecase(source)
                ),
                systemMessageProvider = noOpSystemMessageProvider()
            )
        }
        return OngoingSectionStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = executorFactory
        ).create().also(createdStores::add)
    }

    private fun createAnnouncedStore(source: AnimeListSource): AnnouncedSectionStore {
        val executorFactory: AnnouncedSectionExecutorFactory = {
            AnnouncedSectionExecutorImpl(
                coroutineContextProvider = createCoroutineContextProvider(),
                usecases = AnnouncedUsecases(
                    fetchAnnouncedAnimeListUsecase = FetchAnnouncedAnimeListUsecase(source)
                ),
                systemMessageProvider = noOpSystemMessageProvider()
            )
        }
        return AnnouncedSectionStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = executorFactory
        ).create().also(createdStores::add)
    }

    private fun createSearchStore(source: AnimeListSource): SearchSectionStore {
        val executorFactory: SearchSectionExecutorFactory = {
            SearchSectionExecutorImpl(
                coroutineContextProvider = createCoroutineContextProvider(),
                usecases = SearchUsecases(
                    fetchAnimeListBySearchUsecase = FetchAnimeListBySearchUsecase(source),
                    fetchAnimeDetailsByIdUsecase = FetchAnimeDetailsByIdUsecase(source)
                ),
                systemMessageProvider = noOpSystemMessageProvider()
            )
        }
        return SearchSectionStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = executorFactory
        ).create().also(createdStores::add)
    }

    @Test
    fun aDatabaseStoreStateReachesTheMainStoreAsTheEnabledNotificationIds() =
        runTest(testDispatcher) {
            //Given
            val wiring = createWiring()

            //When
            wiring.database.items.value = listOf(testDbItem(id = 1), testDbItem(id = 7))
            runCurrent()

            //Then
            assertEquals(setOf(1, 7), wiring.mainStore.state.enabledNotificationIds)
        }

    @Test
    fun aDatabaseStoreStateReachesTheViewAsARenderedUiModel() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring()
        wiring.ongoingSectionStore.accept(OngoingSectionStore.Intent.OpenSection)
        runCurrent()

        //When
        wiring.database.items.value = listOf(testDbItem(id = 1))
        runCurrent()

        //Then
        val listItems = wiring.view.renderedModels.last().listContent.listItems
        assertEquals(1, listItems.size)
        assertEquals("Frieren", listItems.single().name)
        assertEquals(NotificationUi.ENABLED, listItems.single().notification)
    }

    @Test
    fun theOngoingSectionsStateReachesTheMainStoreAsItsOngoingContent() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring()

        //When
        wiring.ongoingSectionStore.accept(OngoingSectionStore.Intent.OpenSection)
        runCurrent()

        //Then
        assertEquals(
            listOf("Frieren"),
            wiring.mainStore.state.ongoingContent.listItems.map(ListItemDomain::name)
        )
    }

    @Test
    fun theAnnouncedSectionsStateReachesTheMainStoreAsItsAnnouncedContent() =
        runTest(testDispatcher) {
            //Given
            val wiring = createWiring()

            //When
            wiring.announcedSectionStore.accept(AnnouncedSectionStore.Intent.OpenSection)
            runCurrent()

            //Then
            assertEquals(
                listOf("Dandadan"),
                wiring.mainStore.state.announcedContent.listItems.map(ListItemDomain::name)
            )
        }

    @Test
    fun theSearchSectionsStateReachesTheMainStoreAsItsSearchContent() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring()

        //When
        wiring.searchSectionStore.accept(SearchSectionStore.Intent.OpenSection)
        runCurrent()

        //Then
        assertEquals(
            listOf("Bleach"),
            wiring.mainStore.state.searchContent.listItems.map(ListItemDomain::name)
        )
    }

    @Test
    fun aViewEventReachesTheMainStore() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring()

        //When
        wiring.view.dispatch(AnimeListMainStore.Intent.ChangeSearchText(searchText = "totoro"))
        runCurrent()

        //Then
        assertEquals("totoro", wiring.mainStore.state.search.searchText)
    }

    @Test
    fun aMainStoreLabelReachesTheSectionStoreItNames() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring()

        //When
        wiring.view.dispatch(AnimeListMainStore.Intent.AnnouncedSectionClick)
        runCurrent()

        //Then
        assertEquals(
            listOf("Dandadan"),
            wiring.announcedSectionStore.state.sectionContent.listItems.map(ListItemDomain::name)
        )
        assertEquals(
            emptyList(),
            wiring.ongoingSectionStore.state.sectionContent.listItems,
            "the announced tab must not open the ongoing section too"
        )
        assertEquals(SectionHatUi.ANNOUNCED, wiring.view.renderedModels.last().selectedSection)
    }

    @Test
    fun aNotificationTapOnAnUnsavedItemReachesTheDatabaseStoreAsAnInsert() =
        runTest(testDispatcher) {
            //Given
            val wiring = createWiring()
            wiring.ongoingSectionStore.accept(OngoingSectionStore.Intent.OpenSection)
            runCurrent()

            //When
            wiring.view.dispatch(AnimeListMainStore.Intent.NotificationClick(id = 1))
            runCurrent()

            //Then
            assertEquals(
                listOf(1),
                wiring.database.insertedItems.map(AnimeDbDomain::id)
            )
            assertEquals(emptyList(), wiring.database.deletedIds)
        }

    @Test
    fun aNotificationTapOnASavedItemReachesTheDatabaseStoreAsADelete() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring(databaseItems = listOf(testDbItem(id = 1)))
        wiring.ongoingSectionStore.accept(OngoingSectionStore.Intent.OpenSection)
        runCurrent()

        //When
        wiring.view.dispatch(AnimeListMainStore.Intent.NotificationClick(id = 1))
        runCurrent()

        //Then
        assertEquals(listOf(1), wiring.database.deletedIds)
        assertEquals(emptyList(), wiring.database.insertedItems)
    }

    @Test
    fun theSearchSectionsResetListPositionLabelReachesTheMainStore() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring()

        //When
        wiring.searchSectionStore.accept(SearchSectionStore.Intent.UpdateSection)
        runCurrent()

        //Then
        assertTrue(
            wiring.mainStore.state.isNeedToResetListPositon,
            "the search section's reset request never reached the main store"
        )
    }

    @Test
    fun destroyingTheLifecycleDisposesAllFiveStores() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring()

        //When
        wiring.lifecycle.destroy()

        //Then
        assertTrue(wiring.mainStore.isDisposed, "the main store outlived its screen")
        assertTrue(wiring.animeDatabaseStore.isDisposed, "the database store outlived its screen")
        assertTrue(
            wiring.ongoingSectionStore.isDisposed,
            "the ongoing section store outlived its screen"
        )
        assertTrue(
            wiring.announcedSectionStore.isDisposed,
            "the announced section store outlived its screen"
        )
        assertTrue(
            wiring.searchSectionStore.isDisposed,
            "the search section store outlived its screen"
        )
    }
}
