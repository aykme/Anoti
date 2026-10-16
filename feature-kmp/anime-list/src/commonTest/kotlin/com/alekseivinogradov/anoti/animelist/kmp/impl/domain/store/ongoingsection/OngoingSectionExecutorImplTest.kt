package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.ongoingsection

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.RESTORED_SECTION_MAX_ITEM_COUNT
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.data.source.fake.AnimeListSourceFake
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchOngoingAnimeListUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper.OngoingUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.fake.CoroutineContextProviderFake
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
        onConnectionErrorSystemMessage: () -> Unit = {},
        onUnknownErrorSystemMessage: () -> Unit = {}
    ): OngoingSectionStore {
        val source = AnimeListSourceFake(
            ongoing = { page, _ ->
                beforeOngoingResult(page)
                pages[page] ?: CallResult.Success(emptyList())
            }
        )
        val coroutineContextProvider = CoroutineContextProviderFake()
        val usecases = OngoingUsecases(
            fetchOngoingAnimeListUsecase = FetchOngoingAnimeListUsecase(source)
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
    fun aNextPageRepeatingALoadedItemKeepsItOnce() = runTest(testDispatcher) {
        //Given
        // The ranking can shift between two requests and repeat an item on the next page.
        val firstItem = testListItem(id = 1)
        val secondItem = testListItem(id = 2)
        val store = createStore(
            pages = mapOf(
                1 to CallResult.Success(listOf(firstItem)),
                2 to CallResult.Success(listOf(firstItem, secondItem))
            )
        )
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(OngoingSectionStore.Intent.LoadNextPage)
        store.states.first { it.sectionContent.listItems.size >= 2 }

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
        runCurrent()

        //Then
        assertEquals(1, systemMessageCount)
        assertEquals(listOf(item), store.state.sectionContent.listItems)
        assertEquals(ContentTypeDomain.LOADED, store.state.sectionContent.contentType)
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
            beforeOngoingResult = { page: Int -> requestedPages.add(page) }
        )
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }
        store.accept(OngoingSectionStore.Intent.LoadNextPage)
        store.states.first { it.sectionContent.listItems.size == 1 }

        //When
        store.accept(OngoingSectionStore.Intent.LoadNextPage)

        //Then
        assertEquals(listOf(1, 2), requestedPages)
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
    fun aSecondLoadNextPageKeepsTheFirstOneWithinTheReachOfARefresh() = runTest(testDispatcher) {
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
                enabledExtraEpisodesInfoIds = setOf()
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
    fun openSectionOnFirstPageOtherErrorMarksErrorAndShowsUnknownMessage() =
        runTest(testDispatcher) {
            //Given
            var unknownErrorCount = 0
            val store = createStore(
                pages = mapOf(1 to CallResult.OtherError(throwable = Throwable())),
                onUnknownErrorSystemMessage = { unknownErrorCount++ }
            )

            //When
            store.accept(OngoingSectionStore.Intent.OpenSection)
            store.states.first { it.sectionContent.contentType == ContentTypeDomain.ERROR }

            //Then
            assertEquals(1, unknownErrorCount)
            assertTrue(store.state.sectionContent.listItems.isEmpty())
        }

    @Test
    fun loadNextPageOnAnUnexpectedErrorShowsTheUnknownMessageAndKeepsTheList() =
        runTest(testDispatcher) {
            //Given
            var unknownErrorCount = 0
            val item = testListItem(id = 1)
            val store = createStore(
                pages = mapOf(
                    1 to CallResult.Success(listOf(item)),
                    2 to CallResult.OtherError(throwable = Throwable())
                ),
                onUnknownErrorSystemMessage = { unknownErrorCount++ }
            )
            store.accept(OngoingSectionStore.Intent.OpenSection)
            store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

            //When
            store.accept(OngoingSectionStore.Intent.LoadNextPage)
            runCurrent()

            //Then
            assertEquals(1, unknownErrorCount)
            assertEquals(listOf(item), store.state.sectionContent.listItems)
            assertEquals(ContentTypeDomain.LOADED, store.state.sectionContent.contentType)
        }

    @Test
    fun aRestoreStoppedByAPagingErrorKeepsItsTargetForTheNextAttempt() = runTest(testDispatcher) {
        //Given
        var connectionErrorCount = 0
        val firstPageItems = listOf(testListItem(id = 1), testListItem(id = 2))
        val store = createStore(
            pages = mapOf(
                1 to CallResult.Success(firstPageItems),
                2 to CallResult.HttpError(code = 500, throwable = Throwable())
            ),
            onConnectionErrorSystemMessage = { connectionErrorCount++ }
        )
        store.accept(
            OngoingSectionStore.Intent.RestoreSection(
                itemCount = 4,
                enabledExtraEpisodesInfoIds = setOf()
            )
        )

        //When
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.ERROR }

        //Then
        assertEquals(1, connectionErrorCount)
        assertEquals(4, store.state.restoreTargetItemCount)
    }

    @Test
    fun aRestoreStoppedByAnUnexpectedErrorShowsTheUnknownMessage() = runTest(testDispatcher) {
        //Given
        var unknownErrorCount = 0
        val store = createStore(
            pages = mapOf(
                1 to CallResult.Success(listOf(testListItem(id = 1), testListItem(id = 2))),
                2 to CallResult.OtherError(throwable = Throwable())
            ),
            onUnknownErrorSystemMessage = { unknownErrorCount++ }
        )
        store.accept(
            OngoingSectionStore.Intent.RestoreSection(
                itemCount = 4,
                enabledExtraEpisodesInfoIds = setOf()
            )
        )

        //When
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.ERROR }

        //Then
        assertEquals(1, unknownErrorCount)
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
                    enabledExtraEpisodesInfoIds = setOf(1)
                )
            )

            //When
            store.accept(OngoingSectionStore.Intent.OpenSection)
            store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

            //Then
            assertEquals(firstPageItems + secondPageItems, store.state.sectionContent.listItems)
            assertEquals(listOf(1, 2), requestedPages)
            assertEquals(setOf(1), store.state.sectionContent.enabledExtraEpisodesInfoIds)
            assertNull(store.state.restoreTargetItemCount)
        }

    @Test
    fun aRestoredPageRepeatingALoadedItemKeepsItOnce() = runTest(testDispatcher) {
        //Given
        val firstItem = testListItem(id = 1)
        val secondItem = testListItem(id = 2)
        val store = createStore(
            pages = mapOf(
                1 to CallResult.Success(listOf(firstItem)),
                2 to CallResult.Success(listOf(firstItem, secondItem))
            )
        )
        store.accept(
            OngoingSectionStore.Intent.RestoreSection(
                itemCount = 2,
                enabledExtraEpisodesInfoIds = emptySet()
            )
        )

        //When
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //Then
        assertEquals(listOf(firstItem, secondItem), store.state.sectionContent.listItems)
    }

    @Test
    fun aRestoredItemCountAboveTheCapStopsPagingAtTheCap() = runTest(testDispatcher) {
        //Given
        // Four pages land exactly on the cap: a fifth would mean it is not reached at it.
        val pageSize = RESTORED_SECTION_MAX_ITEM_COUNT / 4
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
                enabledExtraEpisodesInfoIds = setOf()
            )
        )

        //When
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //Then
        assertEquals(listOf(1, 2, 3, 4), requestedPages)
        assertEquals(pageSize * 4, store.state.sectionContent.listItems.size)
    }

    @Test
    fun aRefreshAfterARestoreClearsTheRestoredToggles() = runTest(testDispatcher) {
        //Given
        val item = testListItem(id = 1)
        val store = createStore(pages = mapOf(1 to CallResult.Success(listOf(item))))
        store.accept(
            OngoingSectionStore.Intent.RestoreSection(
                itemCount = 1,
                enabledExtraEpisodesInfoIds = setOf(item.id)
            )
        )
        store.accept(OngoingSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(OngoingSectionStore.Intent.UpdateSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //Then
        assertEquals(emptySet(), store.state.sectionContent.enabledExtraEpisodesInfoIds)
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
