package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.searchsection

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.SEARCH_DEBOUNCE_MILLISECONDS
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnimeListBySearchUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper.SearchUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.fake.CoroutineContextProviderFake
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchSectionExecutorImplTest {

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

    private class FakeSearchSource(
        private val pages: Map<Int, CallResult<List<ListItemDomain>>>,
        private val beforeSearchResult: suspend (page: Int, search: String) -> Unit = { _, _ -> },
        private val details: suspend (AnimeId) -> CallResult<ListItemDomain> = {
            error("no details source in this test")
        }
    ) : AnimeListSource {
        override suspend fun getOngoingList(page: Int, sort: SortData): CallResult<List<ListItemDomain>> {
            error("not used in SearchSectionExecutorImplTest")
        }

        override suspend fun getAnnouncedList(page: Int, sort: SortData): CallResult<List<ListItemDomain>> {
            error("not used in SearchSectionExecutorImplTest")
        }

        override suspend fun getListBySearch(
            page: Int,
            search: String,
            sort: SortData
        ): CallResult<List<ListItemDomain>> {
            beforeSearchResult(page, search)
            return pages[page] ?: CallResult.Success(emptyList())
        }

        override suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain> = details(id)
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

    private fun createStore(
        pages: Map<Int, CallResult<List<ListItemDomain>>>,
        beforeSearchResult: suspend (page: Int, search: String) -> Unit = { _, _ -> },
        details: suspend (AnimeId) -> CallResult<ListItemDomain> = {
            error("no details source in this test")
        },
        onConnectionErrorSystemMessage: () -> Unit = {},
        onUnknownErrorSystemMessage: () -> Unit = {}
    ): SearchSectionStore {
        val source = FakeSearchSource(pages, beforeSearchResult, details)
        val coroutineContextProvider = CoroutineContextProviderFake()
        val usecases = SearchUsecases(
            fetchAnimeListBySearchUsecase = FetchAnimeListBySearchUsecase(source),
            fetchAnimeDetailsByIdUsecase = FetchAnimeDetailsByIdUsecase(source)
        )
        val systemMessageProvider = SystemMessageProvider(
            makeConnectionErrorSystemMessage = onConnectionErrorSystemMessage,
            makeUnknownErrorSystemMessage = onUnknownErrorSystemMessage
        )
        val executorFactory: SearchSectionExecutorFactory = {
            SearchSectionExecutorImpl(
                coroutineContextProvider = coroutineContextProvider,
                usecases = usecases,
                systemMessageProvider = systemMessageProvider
            )
        }
        return SearchSectionStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = executorFactory
        ).create().also(createdStores::add)
    }

    @Test
    fun updateSectionLoadsFirstPageAndMarksLoaded() = runTest(testDispatcher) {
        //Given
        val item = testListItem(id = 1)
        val store = createStore(pages = mapOf(1 to CallResult.Success(listOf(item))))

        //When
        store.accept(SearchSectionStore.Intent.UpdateSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //Then
        assertEquals(listOf(item), store.state.sectionContent.listItems)
    }

    @Test
    fun loadNextPageAppendsSecondPageItems() = runTest(testDispatcher) {
        //Given
        val firstItem = testListItem(id = 1)
        val secondItem = testListItem(id = 2)
        val store = createStore(
            pages = mapOf(
                1 to CallResult.Success(listOf(firstItem)),
                2 to CallResult.Success(listOf(secondItem))
            )
        )
        store.accept(SearchSectionStore.Intent.UpdateSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(SearchSectionStore.Intent.LoadNextPage)
        store.states.first { it.sectionContent.listItems.size == 2 }

        //Then
        assertEquals(listOf(firstItem, secondItem), store.state.sectionContent.listItems)
    }

    @Test
    fun loadNextPageAtEndOfListDoesNothing() = runTest(testDispatcher) {
        //Given
        val requestedPages = mutableListOf<Int>()
        val item = testListItem(id = 1)
        val store = createStore(
            pages = mapOf(
                1 to CallResult.Success(listOf(item)),
                2 to CallResult.Success(emptyList())
            ),
            beforeSearchResult = { page: Int, _: String -> requestedPages.add(page) }
        )
        store.accept(SearchSectionStore.Intent.UpdateSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }
        store.accept(SearchSectionStore.Intent.LoadNextPage)
        store.states.first { it.sectionContent.listItems.size == 1 }

        //When
        store.accept(SearchSectionStore.Intent.LoadNextPage)

        //Then
        assertEquals(listOf(1, 2), requestedPages)
        assertEquals(listOf(item), store.state.sectionContent.listItems)
    }

    @Test
    fun episodesInfoClickResolvesItemByIdAndTogglesEnabledSet() = runTest(testDispatcher) {
        //Given
        val item = testListItem(id = 1)
        val fetched = item.copy(nextEpisodeAt = "2026-09-10T12:00:00Z")
        val store = createStore(
            pages = mapOf(1 to CallResult.Success(listOf(item))),
            details = { CallResult.Success(fetched) }
        )
        store.accept(SearchSectionStore.Intent.UpdateSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(SearchSectionStore.Intent.EpisodesInfoClick(id = item.id))

        //Then
        assertEquals(setOf(item.id), store.state.sectionContent.enabledExtraEpisodesInfoIds)
        assertEquals(
            mapOf(item.id to fetched.nextEpisodeAt),
            store.state.sectionContent.animeDetails.nextEpisodesInfo
        )

        //When
        store.accept(SearchSectionStore.Intent.EpisodesInfoClick(id = item.id))

        //Then
        assertEquals(emptySet(), store.state.sectionContent.enabledExtraEpisodesInfoIds)
    }

    @Test
    fun episodesInfoClickWithUnknownIdIsNoOp() = runTest(testDispatcher) {
        //Given
        val item = testListItem(id = 1)
        val store = createStore(pages = mapOf(1 to CallResult.Success(listOf(item))))
        store.accept(SearchSectionStore.Intent.UpdateSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(SearchSectionStore.Intent.EpisodesInfoClick(id = 999))

        //Then
        assertTrue(store.state.sectionContent.enabledExtraEpisodesInfoIds.isEmpty())
    }

    @Test
    fun aFailedDetailsFetchShowsTheMessageThatMatchesTheFailure() = runTest(testDispatcher) {
        //Given
        var connectionErrorCount = 0
        var unknownErrorCount = 0
        var detailsCallCount = 0
        val item = testListItem(id = 1)
        val store = createStore(
            pages = mapOf(1 to CallResult.Success(listOf(item))),
            details = {
                detailsCallCount++
                if (detailsCallCount == 1) {
                    CallResult.NetworkError(throwable = Throwable())
                } else {
                    CallResult.OtherError(throwable = Throwable())
                }
            },
            onConnectionErrorSystemMessage = { connectionErrorCount++ },
            onUnknownErrorSystemMessage = { unknownErrorCount++ }
        )
        store.accept(SearchSectionStore.Intent.UpdateSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(SearchSectionStore.Intent.EpisodesInfoClick(id = item.id))
        store.accept(SearchSectionStore.Intent.EpisodesInfoClick(id = item.id))
        store.accept(SearchSectionStore.Intent.EpisodesInfoClick(id = item.id))

        //Then
        assertEquals(1, connectionErrorCount)
        assertEquals(1, unknownErrorCount)
        assertTrue(store.state.sectionContent.animeDetails.nextEpisodesInfo.isEmpty())
    }

    @Test
    fun updateSectionOnFirstPageHttpErrorMarksErrorAndShowsConnectionMessage() = runTest(testDispatcher) {
        //Given
        var connectionErrorCount = 0
        val store = createStore(
            pages = mapOf(1 to CallResult.HttpError(code = 500, throwable = Throwable())),
            onConnectionErrorSystemMessage = { connectionErrorCount++ }
        )

        //When
        store.accept(SearchSectionStore.Intent.UpdateSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.ERROR }

        //Then
        assertEquals(1, connectionErrorCount)
        assertTrue(store.state.sectionContent.listItems.isEmpty())
    }

    @Test
    fun updateSectionOnFirstPageOtherErrorMarksErrorAndShowsUnknownMessage() = runTest(testDispatcher) {
        //Given
        var unknownErrorCount = 0
        val store = createStore(
            pages = mapOf(1 to CallResult.OtherError(throwable = Throwable())),
            onUnknownErrorSystemMessage = { unknownErrorCount++ }
        )

        //When
        store.accept(SearchSectionStore.Intent.UpdateSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.ERROR }

        //Then
        assertEquals(1, unknownErrorCount)
        assertTrue(store.state.sectionContent.listItems.isEmpty())
    }

    @Test
    fun loadNextPageErrorsShowAMessageAndLeaveTheListAlone() = runTest(testDispatcher) {
        //Given
        var connectionErrorCount = 0
        var unknownErrorCount = 0
        val item = testListItem(id = 1)
        val pages = mutableMapOf(
            1 to CallResult.Success(listOf(item)),
            2 to CallResult.HttpError(code = 500, throwable = Throwable())
        )
        val store = createStore(
            pages = pages,
            onConnectionErrorSystemMessage = { connectionErrorCount++ },
            onUnknownErrorSystemMessage = { unknownErrorCount++ }
        )
        store.accept(SearchSectionStore.Intent.UpdateSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(SearchSectionStore.Intent.LoadNextPage)
        pages[2] = CallResult.OtherError(throwable = Throwable())
        store.accept(SearchSectionStore.Intent.LoadNextPage)

        //Then
        assertEquals(1, connectionErrorCount)
        assertEquals(1, unknownErrorCount)
        assertEquals(listOf(item), store.state.sectionContent.listItems)
        assertEquals(ContentTypeDomain.LOADED, store.state.sectionContent.contentType)
    }

    @Test
    fun theDebouncedSearchTextReloadsTheListWithTheNewQuery() = runTest(testDispatcher) {
        //Given
        val searchedQueries = mutableListOf<String>()
        val store = createStore(
            pages = mapOf(1 to CallResult.Success(listOf(testListItem(id = 1)))),
            beforeSearchResult = { _: Int, search: String -> searchedQueries.add(search) }
        )
        store.accept(SearchSectionStore.Intent.OpenSection)
        advanceUntilIdle()

        //When
        store.accept(SearchSectionStore.Intent.ChangeSearchText("Naruto"))
        advanceTimeBy(SEARCH_DEBOUNCE_MILLISECONDS / 2)

        //Then
        assertEquals(listOf(""), searchedQueries)

        //When
        advanceUntilIdle()

        //Then
        assertEquals(listOf("", "Naruto"), searchedQueries)
    }

    @Test
    fun openSectionAfterARestorePagesInTheRestoredItemCount() = runTest(testDispatcher) {
        //Given
        val requestedPages = mutableListOf<Int>()
        val restoredNextEpisodesInfo = mapOf<AnimeId, String?>(1 to "2026-09-10T12:00:00Z")
        val firstPageItems = listOf(testListItem(id = 1), testListItem(id = 2))
        val secondPageItems = listOf(testListItem(id = 3), testListItem(id = 4))
        val store = createStore(
            pages = mapOf(
                1 to CallResult.Success(firstPageItems),
                2 to CallResult.Success(secondPageItems)
            ),
            beforeSearchResult = { page: Int, _: String -> requestedPages.add(page) }
        )
        store.accept(
            SearchSectionStore.Intent.RestoreSection(
                itemCount = 3,
                enabledExtraEpisodesInfoIds = setOf(1),
                nextEpisodesInfo = restoredNextEpisodesInfo
            )
        )

        //When
        store.accept(SearchSectionStore.Intent.OpenSection)
        advanceUntilIdle()

        //Then
        assertEquals(firstPageItems + secondPageItems, store.state.sectionContent.listItems)
        assertEquals(listOf(1, 2), requestedPages)
        assertEquals(setOf(1), store.state.sectionContent.enabledExtraEpisodesInfoIds)
        assertEquals(
            restoredNextEpisodesInfo,
            store.state.sectionContent.animeDetails.nextEpisodesInfo
        )
        assertNull(store.state.restoreTargetItemCount)
    }

    @Test
    fun aRestoreStoppedByAPagingErrorKeepsItsTargetForTheNextAttempt() = runTest(testDispatcher) {
        //Given
        var connectionErrorCount = 0
        val store = createStore(
            pages = mapOf(
                1 to CallResult.Success(listOf(testListItem(id = 1))),
                2 to CallResult.NetworkError(throwable = Throwable())
            ),
            onConnectionErrorSystemMessage = { connectionErrorCount++ }
        )
        store.accept(
            SearchSectionStore.Intent.RestoreSection(
                itemCount = 5,
                enabledExtraEpisodesInfoIds = setOf(),
                nextEpisodesInfo = mapOf()
            )
        )

        //When
        store.accept(SearchSectionStore.Intent.OpenSection)
        advanceUntilIdle()

        //Then
        assertEquals(1, connectionErrorCount)
        assertEquals(ContentTypeDomain.ERROR, store.state.sectionContent.contentType)
        assertTrue(store.state.sectionContent.listItems.isEmpty())
        assertEquals(5, store.state.restoreTargetItemCount)
    }

    @Test
    fun aRestoreStoppedByAnUnexpectedErrorShowsTheUnknownMessage() = runTest(testDispatcher) {
        //Given
        var unknownErrorCount = 0
        val store = createStore(
            pages = mapOf(1 to CallResult.OtherError(throwable = Throwable())),
            onUnknownErrorSystemMessage = { unknownErrorCount++ }
        )
        store.accept(
            SearchSectionStore.Intent.RestoreSection(
                itemCount = 5,
                enabledExtraEpisodesInfoIds = setOf(),
                nextEpisodesInfo = mapOf()
            )
        )

        //When
        store.accept(SearchSectionStore.Intent.OpenSection)
        advanceUntilIdle()

        //Then
        assertEquals(1, unknownErrorCount)
        assertEquals(ContentTypeDomain.ERROR, store.state.sectionContent.contentType)
        assertTrue(store.state.sectionContent.listItems.isEmpty())
    }

    @Test
    fun openSectionsFirstLoadDoesNotResetListPosition() = runTest(testDispatcher) {
        //Given
        val item = testListItem(id = 1)
        val store = createStore(pages = mapOf(1 to CallResult.Success(listOf(item))))
        val emittedLabels = mutableListOf<SearchSectionStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(SearchSectionStore.Intent.OpenSection)
        advanceUntilIdle()

        //Then
        assertEquals(ContentTypeDomain.LOADED, store.state.sectionContent.contentType)
        assertEquals(emptyList(), emittedLabels)
        collectJob.cancel()
    }

    @Test
    fun openSectionWithAlreadyRestoredSearchTextLoadsItWithoutResettingListPosition() = runTest(testDispatcher) {
        //Given
        val item = testListItem(id = 1)
        val requestedQueries = mutableListOf<String>()
        val store = createStore(
            pages = mapOf(1 to CallResult.Success(listOf(item))),
            beforeSearchResult = { _: Int, search: String -> requestedQueries.add(search) }
        )
        val emittedLabels = mutableListOf<SearchSectionStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(SearchSectionStore.Intent.ChangeSearchText("Attack on Titan"))
        store.accept(SearchSectionStore.Intent.OpenSection)
        advanceUntilIdle()

        //Then
        assertEquals(listOf("Attack on Titan"), requestedQueries)
        assertEquals(listOf(item), store.state.sectionContent.listItems)
        assertEquals(emptyList(), emittedLabels)
        collectJob.cancel()
    }

    @Test
    fun changingSearchTextAfterFirstLoadResetsListPositionAgain() = runTest(testDispatcher) {
        //Given
        val item = testListItem(id = 1)
        val store = createStore(pages = mapOf(1 to CallResult.Success(listOf(item))))
        store.accept(SearchSectionStore.Intent.OpenSection)
        advanceUntilIdle()
        val emittedLabels = mutableListOf<SearchSectionStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(SearchSectionStore.Intent.ChangeSearchText("Attack on Titan"))
        advanceUntilIdle()

        //Then
        assertEquals("Attack on Titan", store.state.searchText)
        assertEquals(ContentTypeDomain.LOADED, store.state.sectionContent.contentType)
        assertEquals(
            listOf<SearchSectionStore.Label>(SearchSectionStore.Label.ResetListPositionAfterUpdate),
            emittedLabels
        )
        collectJob.cancel()
    }

    @Test
    fun openingAnAlreadyLoadedSectionLoadsNothingAgain() = runTest(testDispatcher) {
        //Given
        val requestedPages = mutableListOf<Int>()
        val store = createStore(
            pages = mapOf(1 to CallResult.Success(listOf(testListItem(id = 1)))),
            beforeSearchResult = { page: Int, _: String -> requestedPages.add(page) }
        )
        store.accept(SearchSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(SearchSectionStore.Intent.OpenSection)
        advanceUntilIdle()

        //Then
        assertEquals(listOf(1), requestedPages)
    }

    @Test
    fun reopeningTheSectionDoesNotAddASecondSearchTextCollector() = runTest(testDispatcher) {
        //Given
        val requestedQueries = mutableListOf<String>()
        val store = createStore(
            pages = mapOf(1 to CallResult.Success(listOf(testListItem(id = 1)))),
            beforeSearchResult = { _: Int, search: String -> requestedQueries.add(search) }
        )
        store.accept(SearchSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }
        // Leaving the section and coming back subscribes again; a second collector would turn
        // every later keystroke into two identical searches.
        store.accept(SearchSectionStore.Intent.OpenSection)
        advanceUntilIdle()
        requestedQueries.clear()

        //When
        store.accept(SearchSectionStore.Intent.ChangeSearchText("bleach"))
        advanceTimeBy(SEARCH_DEBOUNCE_MILLISECONDS * 2)
        advanceUntilIdle()

        //Then
        assertEquals(listOf("bleach"), requestedQueries)
    }

    @Test
    fun aRestoredNextEpisodeDateIsNotFetchedAgain() = runTest(testDispatcher) {
        //Given
        var detailsCallCount = 0
        val item = testListItem(id = 1)
        val store = createStore(
            pages = mapOf(1 to CallResult.Success(listOf(item))),
            details = {
                detailsCallCount++
                CallResult.Success(item)
            }
        )
        store.accept(
            SearchSectionStore.Intent.RestoreSection(
                itemCount = 1,
                enabledExtraEpisodesInfoIds = setOf(),
                nextEpisodesInfo = mapOf(item.id to "2026-09-10T12:00:00Z")
            )
        )
        store.accept(SearchSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(SearchSectionStore.Intent.EpisodesInfoClick(id = item.id))
        advanceUntilIdle()

        //Then
        assertEquals(0, detailsCallCount)
    }

    @Test
    fun aSecondLoadNextPageKeepsTheFirstOneWithinTheReachOfAReload() = runTest(testDispatcher) {
        //Given
        // A second request while one is in flight must not take over the slot that tracks it:
        // the reload below cancels whatever that slot holds, and the real load would survive.
        val firstItem = testListItem(id = 1)
        val stalePageItem = testListItem(id = 2)
        val reloadedItem = testListItem(id = 3)
        val secondPageArrival = CompletableDeferred<Unit>()
        val pages = mutableMapOf<Int, CallResult<List<ListItemDomain>>>(
            1 to CallResult.Success(listOf(firstItem)),
            2 to CallResult.Success(listOf(stalePageItem))
        )
        val store = createStore(
            pages = pages,
            beforeSearchResult = { page: Int, _: String ->
                if (page == 2) secondPageArrival.await()
            }
        )
        store.accept(SearchSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }
        store.accept(SearchSectionStore.Intent.LoadNextPage)
        store.accept(SearchSectionStore.Intent.LoadNextPage)
        pages[1] = CallResult.Success(listOf(reloadedItem))

        //When
        store.accept(SearchSectionStore.Intent.UpdateSection)
        secondPageArrival.complete(Unit)
        advanceUntilIdle()

        //Then
        assertEquals(listOf(reloadedItem), store.state.sectionContent.listItems)
    }

    @Test
    fun disposingTheStoreCancelsAnInFlightDetailsFetch() = runTest(testDispatcher) {
        //Given
        var detailsFetchWasCancelled = false
        val item = testListItem(id = 1)
        val store = createStore(
            pages = mapOf(1 to CallResult.Success(listOf(item))),
            details = {
                try {
                    awaitCancellation()
                } finally {
                    detailsFetchWasCancelled = true
                }
            }
        )
        store.accept(SearchSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }
        store.accept(SearchSectionStore.Intent.EpisodesInfoClick(id = item.id))

        //When
        store.dispose()

        //Then
        assertTrue(detailsFetchWasCancelled, "the details fetch outlived its store")
    }

    @Test
    fun disposingTheStoreCancelsAnInFlightSectionLoad() = runTest(testDispatcher) {
        //Given
        var loadWasCancelled = false
        val store = createStore(
            pages = mapOf(),
            beforeSearchResult = { _: Int, _: String ->
                try {
                    awaitCancellation()
                } finally {
                    loadWasCancelled = true
                }
            }
        )
        store.accept(SearchSectionStore.Intent.UpdateSection)

        //When
        store.dispose()

        //Then
        assertTrue(loadWasCancelled, "the load outlived its store")
    }
}
