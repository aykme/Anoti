package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.navigation

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SearchDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionHatDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
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
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderBase
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ApplyRestoredMainStateTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private object EmptySource : AnimeListSource {
        override suspend fun getOngoingList(
            page: Int,
            sort: SortData
        ): CallResult<List<ListItemDomain>> = CallResult.Success(emptyList())

        override suspend fun getAnnouncedList(
            page: Int,
            sort: SortData
        ): CallResult<List<ListItemDomain>> = CallResult.Success(emptyList())

        override suspend fun getListBySearch(
            page: Int,
            search: String,
            sort: SortData
        ): CallResult<List<ListItemDomain>> = CallResult.Success(emptyList())

        override suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain> {
            error("not used in ApplyRestoredMainStateTest")
        }
    }

    private class PagedSource(
        private val pages: Map<Int, List<ListItemDomain>>
    ) : AnimeListSource {
        override suspend fun getOngoingList(
            page: Int,
            sort: SortData
        ): CallResult<List<ListItemDomain>> = CallResult.Success(pages[page] ?: emptyList())

        override suspend fun getAnnouncedList(
            page: Int,
            sort: SortData
        ): CallResult<List<ListItemDomain>> = CallResult.Success(pages[page] ?: emptyList())

        override suspend fun getListBySearch(
            page: Int,
            search: String,
            sort: SortData
        ): CallResult<List<ListItemDomain>> = CallResult.Success(pages[page] ?: emptyList())

        override suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain> {
            error("not used in ApplyRestoredMainStateTest")
        }
    }

    private fun testListItem(id: AnimeId) = ListItemDomain(
        id = id,
        name = "Item $id",
        imageUrl = null,
        episodesAired = 1,
        episodesTotal = 12,
        nextEpisodeAt = null,
        airedOn = null,
        releasedOn = null,
        score = 8.0F,
        releaseStatus = ReleaseStatusDomain.ONGOING
    )

    private fun restoredSectionState(
        itemCount: Int = 0,
        enabledExtraEpisodesInfoIds: Set<AnimeId> = setOf(),
        nextEpisodesInfo: Map<AnimeId, String?> = mapOf()
    ) = RestoredSectionState(
        itemCount = itemCount,
        enabledExtraEpisodesInfoIds = enabledExtraEpisodesInfoIds,
        nextEpisodesInfo = nextEpisodesInfo
    )

    private fun restoredMainState(
        selectedSection: SectionHatDomain,
        searchType: SearchDomain.Type = SearchDomain.Type.SHOWN,
        searchText: String = "",
        ongoing: RestoredSectionState = restoredSectionState(),
        announced: RestoredSectionState = restoredSectionState(),
        search: RestoredSectionState = restoredSectionState()
    ) = RestoredMainState(
        selectedSection = selectedSection,
        searchType = searchType,
        searchText = searchText,
        ongoing = ongoing,
        announced = announced,
        search = search
    )

    private fun createCoroutineContextProvider() = object : CoroutineContextProviderBase() {
        override val exceptionHandlerCallback: (Throwable) -> Unit = {}
    }

    private fun createMainStore(): AnimeListMainStore {
        val executorFactory: AnimeListExecutorFactory = {
            AnimeListExecutorImpl(coroutineContextProvider = createCoroutineContextProvider())
        }
        return AnimeListMainStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = executorFactory
        ).create()
    }

    private fun createOngoingStore(source: AnimeListSource = EmptySource): OngoingSectionStore {
        val usecases = OngoingUsecases(
            fetchOngoingAnimeListUsecase = FetchOngoingAnimeListUsecase(source),
            fetchAnimeDetailsByIdUsecase = FetchAnimeDetailsByIdUsecase(source)
        )
        val executorFactory: OngoingSectionExecutorFactory = {
            OngoingSectionExecutorImpl(
                coroutineContextProvider = createCoroutineContextProvider(),
                usecases = usecases,
                toastProvider = ToastProvider(makeConnectionErrorToast = {}, makeUnknownErrorToast = {})
            )
        }
        return OngoingSectionStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = executorFactory
        ).create()
    }

    private fun createAnnouncedStore(source: AnimeListSource = EmptySource): AnnouncedSectionStore {
        val usecases = AnnouncedUsecases(
            fetchAnnouncedAnimeListUsecase = FetchAnnouncedAnimeListUsecase(source)
        )
        val executorFactory: AnnouncedSectionExecutorFactory = {
            AnnouncedSectionExecutorImpl(
                coroutineContextProvider = createCoroutineContextProvider(),
                usecases = usecases,
                toastProvider = ToastProvider(makeConnectionErrorToast = {}, makeUnknownErrorToast = {})
            )
        }
        return AnnouncedSectionStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = executorFactory
        ).create()
    }

    private fun createSearchStore(source: AnimeListSource = EmptySource): SearchSectionStore {
        val usecases = SearchUsecases(
            fetchAnimeListBySearchUsecase = FetchAnimeListBySearchUsecase(source),
            fetchAnimeDetailsByIdUsecase = FetchAnimeDetailsByIdUsecase(source)
        )
        val executorFactory: SearchSectionExecutorFactory = {
            SearchSectionExecutorImpl(
                coroutineContextProvider = createCoroutineContextProvider(),
                usecases = usecases,
                toastProvider = ToastProvider(makeConnectionErrorToast = {}, makeUnknownErrorToast = {})
            )
        }
        return SearchSectionStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = executorFactory
        ).create()
    }

    @Test
    fun nullRestoredStateOpensOnlyOngoingsSection() = runTest(testDispatcher) {
        //Given
        val mainStore = createMainStore()
        val ongoingStore = createOngoingStore()
        val announcedStore = createAnnouncedStore()
        val searchStore = createSearchStore()

        //When
        applyRestoredMainState(
            restoredState = null,
            mainStore = mainStore,
            ongoingSectionStore = ongoingStore,
            announcedSectionStore = announcedStore,
            searchSectionStore = searchStore
        )
        ongoingStore.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //Then
        assertEquals(SectionHatDomain.ONGOINGS, mainStore.state.selectedSection)
        assertEquals(ContentTypeDomain.LOADED, ongoingStore.state.sectionContent.contentType)
        assertEquals(ContentTypeDomain.LOADING, announcedStore.state.sectionContent.contentType)
        assertEquals(ContentTypeDomain.LOADING, searchStore.state.sectionContent.contentType)
    }

    @Test
    fun ongoingsRestoredStateOpensOnlyOngoingsSection() = runTest(testDispatcher) {
        //Given
        val mainStore = createMainStore()
        val ongoingStore = createOngoingStore()
        val announcedStore = createAnnouncedStore()
        val searchStore = createSearchStore()

        //When
        applyRestoredMainState(
            restoredState = restoredMainState(SectionHatDomain.ONGOINGS, searchText = ""),
            mainStore = mainStore,
            ongoingSectionStore = ongoingStore,
            announcedSectionStore = announcedStore,
            searchSectionStore = searchStore
        )
        ongoingStore.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //Then
        assertEquals(SectionHatDomain.ONGOINGS, mainStore.state.selectedSection)
        assertEquals(ContentTypeDomain.LOADED, ongoingStore.state.sectionContent.contentType)
        assertEquals(ContentTypeDomain.LOADING, announcedStore.state.sectionContent.contentType)
        assertEquals(ContentTypeDomain.LOADING, searchStore.state.sectionContent.contentType)
    }

    @Test
    fun ongoingsRestoredStateStillReplaysLeftoverSearchTextToSearchStore() = runTest(testDispatcher) {
        //Given
        val mainStore = createMainStore()
        val ongoingStore = createOngoingStore()
        val announcedStore = createAnnouncedStore()
        val searchStore = createSearchStore()

        //When
        applyRestoredMainState(
            restoredState = restoredMainState(SectionHatDomain.ONGOINGS, searchText = "totoro"),
            mainStore = mainStore,
            ongoingSectionStore = ongoingStore,
            announcedSectionStore = announcedStore,
            searchSectionStore = searchStore
        )

        //Then
        assertEquals(SectionHatDomain.ONGOINGS, mainStore.state.selectedSection)
        assertEquals("totoro", mainStore.state.search.searchText)
        assertEquals("totoro", searchStore.state.searchText)
        assertEquals(ContentTypeDomain.LOADING, searchStore.state.sectionContent.contentType)
    }

    @Test
    fun announcedRestoredStateSelectsSectionAndOpensAnnouncedStoreOnly() = runTest(testDispatcher) {
        //Given
        val mainStore = createMainStore()
        val ongoingStore = createOngoingStore()
        val announcedStore = createAnnouncedStore()
        val searchStore = createSearchStore()

        //When
        applyRestoredMainState(
            restoredState = restoredMainState(SectionHatDomain.ANNOUNCED, searchText = ""),
            mainStore = mainStore,
            ongoingSectionStore = ongoingStore,
            announcedSectionStore = announcedStore,
            searchSectionStore = searchStore
        )
        announcedStore.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //Then
        assertEquals(SectionHatDomain.ANNOUNCED, mainStore.state.selectedSection)
        assertEquals(ContentTypeDomain.LOADED, announcedStore.state.sectionContent.contentType)
        assertEquals(ContentTypeDomain.LOADING, ongoingStore.state.sectionContent.contentType)
        assertEquals(false, mainStore.state.isNeedToResetListPositon)
    }

    @Test
    fun searchRestoredStateWithTextDispatchesTextDirectlyToSearchStore() = runTest(testDispatcher) {
        //Given
        val mainStore = createMainStore()
        val ongoingStore = createOngoingStore()
        val announcedStore = createAnnouncedStore()
        val searchStore = createSearchStore()

        //When
        applyRestoredMainState(
            restoredState = restoredMainState(SectionHatDomain.SEARCH, searchText = "totoro"),
            mainStore = mainStore,
            ongoingSectionStore = ongoingStore,
            announcedSectionStore = announcedStore,
            searchSectionStore = searchStore
        )

        //Then
        assertEquals(SectionHatDomain.SEARCH, mainStore.state.selectedSection)
        assertEquals("totoro", mainStore.state.search.searchText)
        assertEquals("totoro", searchStore.state.searchText)
        assertEquals(ContentTypeDomain.LOADING, ongoingStore.state.sectionContent.contentType)
        assertEquals(false, mainStore.state.isNeedToResetListPositon)
    }

    @Test
    fun searchRestoredStateWithBlankTextOpensSectionWithoutChangingSearchText() = runTest(testDispatcher) {
        //Given
        val mainStore = createMainStore()
        val ongoingStore = createOngoingStore()
        val announcedStore = createAnnouncedStore()
        val searchStore = createSearchStore()

        //When
        applyRestoredMainState(
            restoredState = restoredMainState(SectionHatDomain.SEARCH, searchText = ""),
            mainStore = mainStore,
            ongoingSectionStore = ongoingStore,
            announcedSectionStore = announcedStore,
            searchSectionStore = searchStore
        )

        //Then
        assertEquals(SectionHatDomain.SEARCH, mainStore.state.selectedSection)
        assertEquals("", searchStore.state.searchText)
        assertEquals(ContentTypeDomain.LOADING, ongoingStore.state.sectionContent.contentType)
        assertEquals(false, mainStore.state.isNeedToResetListPositon)
    }

    @Test
    fun ongoingsRestoredStatePagesInSavedItemCountAcrossMultiplePages() = runTest(testDispatcher) {
        //Given
        val pages = mapOf(
            1 to listOf(testListItem(1), testListItem(2)),
            2 to listOf(testListItem(3), testListItem(4)),
            3 to listOf(testListItem(5), testListItem(6))
        )
        val mainStore = createMainStore()
        val ongoingStore = createOngoingStore(source = PagedSource(pages))
        val announcedStore = createAnnouncedStore()
        val searchStore = createSearchStore()

        //When
        applyRestoredMainState(
            restoredState = restoredMainState(SectionHatDomain.ONGOINGS, ongoing = restoredSectionState(itemCount = 5)),
            mainStore = mainStore,
            ongoingSectionStore = ongoingStore,
            announcedSectionStore = announcedStore,
            searchSectionStore = searchStore
        )
        ongoingStore.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //Then
        // Pages in whole pages until the saved count is covered (5 needs all 3 two-item pages).
        assertEquals(6, ongoingStore.state.sectionContent.listItems.size)
        assertEquals(null, ongoingStore.state.restoreTargetItemCount)
    }

    @Test
    fun ongoingsRestoredStateCapsPagedInItemCountAtTheMaximum() = runTest(testDispatcher) {
        //Given
        val pages = (1..10).associateWith { page -> listOf(testListItem(page)) }
        val mainStore = createMainStore()
        val ongoingStore = createOngoingStore(source = PagedSource(pages))
        val announcedStore = createAnnouncedStore()
        val searchStore = createSearchStore()

        //When
        applyRestoredMainState(
            restoredState = restoredMainState(SectionHatDomain.ONGOINGS, ongoing = restoredSectionState(itemCount = 1000)),
            mainStore = mainStore,
            ongoingSectionStore = ongoingStore,
            announcedSectionStore = announcedStore,
            searchSectionStore = searchStore
        )
        ongoingStore.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //Then
        assertEquals(10, ongoingStore.state.sectionContent.listItems.size)
    }

    @Test
    fun restoredStateAppliesEachSectionsDisplayStateRegardlessOfWhichIsSelected() = runTest(testDispatcher) {
        //Given
        val mainStore = createMainStore()
        val ongoingStore = createOngoingStore()
        val announcedStore = createAnnouncedStore()
        val searchStore = createSearchStore()

        //When
        applyRestoredMainState(
            restoredState = restoredMainState(
                SectionHatDomain.SEARCH,
                ongoing = restoredSectionState(
                    enabledExtraEpisodesInfoIds = setOf(1),
                    nextEpisodesInfo = mapOf(1 to "2026-09-10T12:00:00Z")
                ),
                announced = restoredSectionState(enabledExtraEpisodesInfoIds = setOf(2))
            ),
            mainStore = mainStore,
            ongoingSectionStore = ongoingStore,
            announcedSectionStore = announcedStore,
            searchSectionStore = searchStore
        )

        //Then
        // Applied immediately even though neither Ongoing nor Announced is the selected section.
        assertEquals(setOf(1), ongoingStore.state.sectionContent.enabledExtraEpisodesInfoIds)
        assertEquals(
            mapOf(1 to "2026-09-10T12:00:00Z"),
            ongoingStore.state.sectionContent.animeDetails.nextEpisodesInfo
        )
        assertEquals(setOf(2), announcedStore.state.sectionContent.enabledExtraEpisodesInfoIds)
    }

    @Test
    fun searchRestoredStateWithHiddenTypeCancelsTheSearchBarAfterOpeningIt() = runTest(testDispatcher) {
        //Given
        val mainStore = createMainStore()
        val ongoingStore = createOngoingStore()
        val announcedStore = createAnnouncedStore()
        val searchStore = createSearchStore()

        //When
        applyRestoredMainState(
            restoredState = restoredMainState(
                SectionHatDomain.SEARCH,
                searchType = SearchDomain.Type.HIDDEN
            ),
            mainStore = mainStore,
            ongoingSectionStore = ongoingStore,
            announcedSectionStore = announcedStore,
            searchSectionStore = searchStore
        )

        //Then
        assertEquals(SectionHatDomain.SEARCH, mainStore.state.selectedSection)
        assertEquals(SearchDomain.Type.HIDDEN, mainStore.state.search.type)
    }
}
