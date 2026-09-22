package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.ongoingsection

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchOngoingAnimeListUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper.OngoingUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderBase
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OngoingSectionExecutorImplTest {

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

    private class FakeOngoingSource(
        private val pages: Map<Int, CallResult<List<ListItemDomain>>>,
        private val beforeOngoingResult: suspend (page: Int) -> Unit = {},
        private val details: suspend (AnimeId) -> CallResult<ListItemDomain> = {
            error("no details source in this test")
        }
    ) : AnimeListSource {
        override suspend fun getOngoingList(page: Int, sort: SortData): CallResult<List<ListItemDomain>> {
            beforeOngoingResult(page)
            return pages[page] ?: CallResult.Success(emptyList())
        }

        override suspend fun getAnnouncedList(page: Int, sort: SortData): CallResult<List<ListItemDomain>> {
            error("not used in OngoingSectionExecutorImplTest")
        }

        override suspend fun getListBySearch(
            page: Int,
            search: String,
            sort: SortData
        ): CallResult<List<ListItemDomain>> {
            error("not used in OngoingSectionExecutorImplTest")
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
        beforeOngoingResult: suspend (page: Int) -> Unit = {},
        details: suspend (AnimeId) -> CallResult<ListItemDomain> = {
            error("no details source in this test")
        },
        onConnectionErrorSystemMessage: () -> Unit = {},
        onUnknownErrorSystemMessage: () -> Unit = {}
    ): OngoingSectionStore {
        val source = FakeOngoingSource(pages, beforeOngoingResult, details)
        val coroutineContextProvider = object : CoroutineContextProviderBase() {
            override val exceptionHandlerCallback: (Throwable) -> Unit = {}
        }
        val usecases = OngoingUsecases(
            fetchOngoingAnimeListUsecase = FetchOngoingAnimeListUsecase(source),
            fetchAnimeDetailsByIdUsecase = FetchAnimeDetailsByIdUsecase(source)
        )
        val systemMessageProvider = SystemMessageProvider(
            makeConnectionErrorSystemMessage = onConnectionErrorSystemMessage,
            makeUnknownErrorSystemMessage = onUnknownErrorSystemMessage
        )
        val executorFactory: OngoingSectionExecutorFactory = {
            OngoingSectionExecutorImpl(
                coroutineContextProvider = coroutineContextProvider,
                usecases = usecases,
                systemMessageProvider = systemMessageProvider
            )
        }
        return OngoingSectionStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = executorFactory
        ).create().also(createdStores::add)
    }

    @Test
    fun openSectionLoadsFirstPageAndMarksLoaded() = runTest(testDispatcher) {
        //Given
        val item = testListItem(id = 1)
        val store = createStore(pages = mapOf(1 to CallResult.Success(listOf(item))))

        //When
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //Then
        assertEquals(listOf(item), store.state.sectionContent.listItems)
    }

    @Test
    fun openSectionOnFirstPageHttpErrorMarksErrorAndShowsSystemMessage() = runTest(testDispatcher) {
        //Given
        var systemMessageCount = 0
        val store = createStore(
            pages = mapOf(1 to CallResult.HttpError(code = 500, throwable = Throwable())),
            onConnectionErrorSystemMessage = { systemMessageCount++ }
        )

        //When
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.ERROR }

        //Then
        assertEquals(1, systemMessageCount)
        assertTrue(store.state.sectionContent.listItems.isEmpty())
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
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(OngoingSectionStore.Intent.LoadNextPage)
        store.states.first { it.sectionContent.listItems.size == 2 }

        //Then
        assertEquals(listOf(firstItem, secondItem), store.state.sectionContent.listItems)
    }

    @Test
    fun loadNextPageOnHttpErrorLeavesListAndContentTypeUnchanged() = runTest(testDispatcher) {
        //Given
        var systemMessageCount = 0
        val item = testListItem(id = 1)
        val store = createStore(
            pages = mapOf(
                1 to CallResult.Success(listOf(item)),
                2 to CallResult.HttpError(code = 500, throwable = Throwable())
            ),
            onConnectionErrorSystemMessage = { systemMessageCount++ }
        )
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(OngoingSectionStore.Intent.LoadNextPage)
        store.states.first { systemMessageCount == 1 }

        //Then
        assertEquals(listOf(item), store.state.sectionContent.listItems)
        assertEquals(ContentTypeDomain.LOADED, store.state.sectionContent.contentType)
    }

    @Test
    fun loadNextPageAtEndOfListDoesNothing() = runTest(testDispatcher) {
        //Given
        val item = testListItem(id = 1)
        val store = createStore(
            pages = mapOf(
                1 to CallResult.Success(listOf(item)),
                2 to CallResult.Success(emptyList())
            )
        )
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }
        store.accept(OngoingSectionStore.Intent.LoadNextPage)
        store.states.first { it.sectionContent.listItems.size == 1 }

        //When
        store.accept(OngoingSectionStore.Intent.LoadNextPage)

        //Then
        assertEquals(listOf(item), store.state.sectionContent.listItems)
    }

    @Test
    fun episodesInfoClickResolvesItemByIdAndTogglesEnabledSet() = runTest(testDispatcher) {
        //Given
        val item = testListItem(id = 1)
        val store = createStore(pages = mapOf(1 to CallResult.Success(listOf(item))))
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(OngoingSectionStore.Intent.EpisodesInfoClick(id = item.id))

        //Then
        assertEquals(setOf(item.id), store.state.sectionContent.enabledExtraEpisodesInfoIds)

        //When
        store.accept(OngoingSectionStore.Intent.EpisodesInfoClick(id = item.id))

        //Then
        assertEquals(emptySet(), store.state.sectionContent.enabledExtraEpisodesInfoIds)
    }

    @Test
    fun episodesInfoClickWithUnknownIdIsNoOp() = runTest(testDispatcher) {
        //Given
        val item = testListItem(id = 1)
        val store = createStore(pages = mapOf(1 to CallResult.Success(listOf(item))))
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(OngoingSectionStore.Intent.EpisodesInfoClick(id = 999))

        //Then
        assertTrue(store.state.sectionContent.enabledExtraEpisodesInfoIds.isEmpty())
    }

    @Test
    fun aNextPageStillInFlightWhenARefreshStartsNeverReachesTheList() = runTest(testDispatcher) {
        //Given
        val firstItem = testListItem(id = 1)
        val stalePageItem = testListItem(id = 2)
        val refreshedItem = testListItem(id = 3)
        val secondPageArrival = CompletableDeferred<Unit>()
        val pages = mutableMapOf<Int, CallResult<List<ListItemDomain>>>(
            1 to CallResult.Success(listOf(firstItem)),
            2 to CallResult.Success(listOf(stalePageItem))
        )
        val store = createStore(
            pages = pages,
            beforeOngoingResult = { page: Int -> if (page == 2) secondPageArrival.await() }
        )
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }
        store.accept(OngoingSectionStore.Intent.LoadNextPage)
        pages[1] = CallResult.Success(listOf(refreshedItem))

        //When
        store.accept(OngoingSectionStore.Intent.UpdateSection)
        secondPageArrival.complete(Unit)
        runCurrent()

        //Then
        assertEquals(listOf(refreshedItem), store.state.sectionContent.listItems)
    }

    @Test
    fun aRestoreStillPagingWhenARefreshStartsNeverReachesTheList() = runTest(testDispatcher) {
        //Given
        val restoredItems = listOf(testListItem(id = 1), testListItem(id = 2))
        val stalePageItems = listOf(testListItem(id = 3), testListItem(id = 4))
        val refreshedItem = testListItem(id = 5)
        val secondPageArrival = CompletableDeferred<Unit>()
        val pages = mutableMapOf<Int, CallResult<List<ListItemDomain>>>(
            1 to CallResult.Success(restoredItems),
            2 to CallResult.Success(stalePageItems)
        )
        val store = createStore(
            pages = pages,
            beforeOngoingResult = { page: Int -> if (page == 2) secondPageArrival.await() }
        )
        store.accept(
            OngoingSectionStore.Intent.RestoreSection(
                itemCount = restoredItems.size + stalePageItems.size,
                enabledExtraEpisodesInfoIds = setOf(),
                nextEpisodesInfo = mapOf()
            )
        )
        store.accept(OngoingSectionStore.Intent.OpenSection)
        pages[1] = CallResult.Success(listOf(refreshedItem))

        //When
        store.accept(OngoingSectionStore.Intent.UpdateSection)
        secondPageArrival.complete(Unit)
        runCurrent()

        //Then
        assertEquals(listOf(refreshedItem), store.state.sectionContent.listItems)
    }

    @Test
    fun aSecondDetailsFetchForTheSameItemReplacesTheFirst() = runTest(testDispatcher) {
        //Given
        val item = testListItem(id = 1)
        val fetched = item.copy(nextEpisodeAt = "2026-09-10T12:00:00Z")
        var detailsCallCount = 0
        var firstCallWasCancelled = false
        val store = createStore(
            pages = mapOf(1 to CallResult.Success(listOf(item))),
            details = {
                detailsCallCount++
                if (detailsCallCount == 1) {
                    try {
                        awaitCancellation()
                    } finally {
                        firstCallWasCancelled = true
                    }
                }
                CallResult.Success(fetched)
            }
        )
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }
        store.accept(OngoingSectionStore.Intent.EpisodesInfoClick(id = item.id))
        store.accept(OngoingSectionStore.Intent.EpisodesInfoClick(id = item.id))

        //When
        store.accept(OngoingSectionStore.Intent.EpisodesInfoClick(id = item.id))
        runCurrent()

        //Then
        assertTrue(firstCallWasCancelled, "the replaced fetch outlived its replacement")
        assertEquals(
            mapOf(item.id to fetched.nextEpisodeAt),
            store.state.sectionContent.animeDetails.nextEpisodesInfo
        )
    }

    @Test
    fun openingAnAlreadyLoadedSectionLoadsNothingAgain() = runTest(testDispatcher) {
        //Given
        val requestedPages = mutableListOf<Int>()
        val store = createStore(
            pages = mapOf(1 to CallResult.Success(listOf(testListItem(id = 1)))),
            beforeOngoingResult = { page: Int -> requestedPages.add(page) }
        )
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(OngoingSectionStore.Intent.OpenSection)

        //Then
        assertEquals(listOf(1), requestedPages)
    }

    @Test
    fun openSectionAfterARestorePagesInTheRestoredItemCountAndClearsItsTarget() =
        runTest(testDispatcher) {
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
                beforeOngoingResult = { page: Int -> requestedPages.add(page) }
            )
            store.accept(
                OngoingSectionStore.Intent.RestoreSection(
                    itemCount = 3,
                    enabledExtraEpisodesInfoIds = setOf(1),
                    nextEpisodesInfo = restoredNextEpisodesInfo
                )
            )

            //When
            store.accept(OngoingSectionStore.Intent.OpenSection)
            store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

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
    fun aRestoredItemCountAboveTheCapStopsPagingAtTheCap() = runTest(testDispatcher) {
        //Given
        val pageSize = 30
        val requestedPages = mutableListOf<Int>()
        val pages = (1..5).associateWith { page ->
            CallResult.Success(
                (1..pageSize).map { testListItem(id = (page - 1) * pageSize + it) }
            )
        }
        val store = createStore(
            pages = pages,
            beforeOngoingResult = { page: Int -> requestedPages.add(page) }
        )
        store.accept(
            OngoingSectionStore.Intent.RestoreSection(
                itemCount = pageSize * pages.size,
                enabledExtraEpisodesInfoIds = setOf(),
                nextEpisodesInfo = mapOf()
            )
        )

        //When
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //Then
        assertEquals(listOf(1, 2, 3), requestedPages)
        assertEquals(pageSize * 3, store.state.sectionContent.listItems.size)
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
            OngoingSectionStore.Intent.RestoreSection(
                itemCount = 1,
                enabledExtraEpisodesInfoIds = setOf(),
                nextEpisodesInfo = mapOf(item.id to "2026-09-10T12:00:00Z")
            )
        )
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(OngoingSectionStore.Intent.EpisodesInfoClick(id = item.id))
        runCurrent()

        //Then
        assertEquals(0, detailsCallCount)
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
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }
        store.accept(OngoingSectionStore.Intent.EpisodesInfoClick(id = item.id))

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
            beforeOngoingResult = {
                try {
                    awaitCancellation()
                } finally {
                    loadWasCancelled = true
                }
            }
        )
        store.accept(OngoingSectionStore.Intent.OpenSection)

        //When
        store.dispose()

        //Then
        assertTrue(loadWasCancelled, "the load outlived its store")
    }
}
