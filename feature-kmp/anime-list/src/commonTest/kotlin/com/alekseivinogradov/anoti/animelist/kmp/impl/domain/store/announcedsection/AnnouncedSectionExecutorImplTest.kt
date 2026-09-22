package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.announcedsection

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnnouncedAnimeListUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper.AnnouncedUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderBase
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class AnnouncedSectionExecutorImplTest {

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

    private class FakeAnnouncedSource(
        private val pages: Map<Int, CallResult<List<ListItemDomain>>>,
        private val beforeAnnouncedResult: suspend (page: Int) -> Unit = {}
    ) : AnimeListSource {
        override suspend fun getOngoingList(page: Int, sort: SortData): CallResult<List<ListItemDomain>> {
            error("not used in AnnouncedSectionExecutorImplTest")
        }

        override suspend fun getAnnouncedList(page: Int, sort: SortData): CallResult<List<ListItemDomain>> {
            beforeAnnouncedResult(page)
            return pages[page] ?: CallResult.Success(emptyList())
        }

        override suspend fun getListBySearch(
            page: Int,
            search: String,
            sort: SortData
        ): CallResult<List<ListItemDomain>> {
            error("not used in AnnouncedSectionExecutorImplTest")
        }

        override suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain> {
            error("not used in AnnouncedSectionExecutorImplTest")
        }
    }

    private fun testListItem(id: AnimeId) = ListItemDomain(
        id = id,
        name = "Item $id",
        imageUrl = null,
        episodesAired = null,
        episodesTotal = null,
        nextEpisodeAt = null,
        airedOn = "2026-09-01",
        releasedOn = null,
        score = null,
        releaseStatus = ReleaseStatusDomain.ANNOUNCED
    )

    private fun createStore(
        pages: Map<Int, CallResult<List<ListItemDomain>>>,
        beforeAnnouncedResult: suspend (page: Int) -> Unit = {},
        onConnectionErrorSystemMessage: () -> Unit = {},
        onUnknownErrorSystemMessage: () -> Unit = {}
    ): AnnouncedSectionStore {
        val source = FakeAnnouncedSource(pages, beforeAnnouncedResult)
        val coroutineContextProvider = object : CoroutineContextProviderBase() {
            override val exceptionHandlerCallback: (Throwable) -> Unit = {}
        }
        val usecases = AnnouncedUsecases(
            fetchAnnouncedAnimeListUsecase = FetchAnnouncedAnimeListUsecase(source)
        )
        val systemMessageProvider = SystemMessageProvider(
            makeConnectionErrorSystemMessage = onConnectionErrorSystemMessage,
            makeUnknownErrorSystemMessage = onUnknownErrorSystemMessage
        )
        val executorFactory: AnnouncedSectionExecutorFactory = {
            AnnouncedSectionExecutorImpl(
                coroutineContextProvider = coroutineContextProvider,
                usecases = usecases,
                systemMessageProvider = systemMessageProvider
            )
        }
        return AnnouncedSectionStoreFactory(
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
        store.accept(AnnouncedSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //Then
        assertEquals(listOf(item), store.state.sectionContent.listItems)
    }

    @Test
    fun updateSectionReloadsTheFirstPageAndReplacesTheItems() = runTest(testDispatcher) {
        //Given
        val requestedPages = mutableListOf<Int>()
        val loadedItem = testListItem(id = 1)
        val refreshedItem = testListItem(id = 2)
        val pages = mutableMapOf<Int, CallResult<List<ListItemDomain>>>(
            1 to CallResult.Success(listOf(loadedItem))
        )
        val store = createStore(
            pages = pages,
            beforeAnnouncedResult = { page: Int -> requestedPages.add(page) }
        )
        store.accept(AnnouncedSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }
        pages[1] = CallResult.Success(listOf(refreshedItem))

        //When
        store.accept(AnnouncedSectionStore.Intent.UpdateSection)
        store.states.first { it.sectionContent.listItems == listOf(refreshedItem) }

        //Then
        assertEquals(listOf(1, 1), requestedPages)
        assertEquals(ContentTypeDomain.LOADED, store.state.sectionContent.contentType)
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
        store.accept(AnnouncedSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(AnnouncedSectionStore.Intent.LoadNextPage)
        store.states.first { it.sectionContent.listItems.size == 2 }

        //Then
        assertEquals(listOf(firstItem, secondItem), store.state.sectionContent.listItems)
    }

    @Test
    fun episodesInfoClickResolvesItemByIdAndTogglesEnabledSet() = runTest(testDispatcher) {
        //Given
        val item = testListItem(id = 1)
        val store = createStore(pages = mapOf(1 to CallResult.Success(listOf(item))))
        store.accept(AnnouncedSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(AnnouncedSectionStore.Intent.EpisodesInfoClick(id = item.id))

        //Then
        assertEquals(setOf(item.id), store.state.sectionContent.enabledExtraEpisodesInfoIds)

        //When
        store.accept(AnnouncedSectionStore.Intent.EpisodesInfoClick(id = item.id))

        //Then
        assertEquals(emptySet(), store.state.sectionContent.enabledExtraEpisodesInfoIds)
    }

    @Test
    fun episodesInfoClickWithUnknownIdIsNoOp() = runTest(testDispatcher) {
        //Given
        val item = testListItem(id = 1)
        val store = createStore(pages = mapOf(1 to CallResult.Success(listOf(item))))
        store.accept(AnnouncedSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(AnnouncedSectionStore.Intent.EpisodesInfoClick(id = 999))

        //Then
        assertTrue(store.state.sectionContent.enabledExtraEpisodesInfoIds.isEmpty())
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
            beforeAnnouncedResult = { page: Int -> requestedPages.add(page) }
        )
        store.accept(AnnouncedSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }
        store.accept(AnnouncedSectionStore.Intent.LoadNextPage)
        store.states.first { it.sectionContent.listItems.size == 1 }

        //When
        store.accept(AnnouncedSectionStore.Intent.LoadNextPage)

        //Then
        assertEquals(listOf(1, 2), requestedPages)
        assertEquals(listOf(item), store.state.sectionContent.listItems)
    }

    @Test
    fun openSectionOnFirstPageHttpErrorMarksErrorAndShowsConnectionMessage() = runTest(testDispatcher) {
        //Given
        var connectionErrorCount = 0
        val store = createStore(
            pages = mapOf(1 to CallResult.HttpError(code = 500, throwable = Throwable())),
            onConnectionErrorSystemMessage = { connectionErrorCount++ }
        )

        //When
        store.accept(AnnouncedSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.ERROR }

        //Then
        assertEquals(1, connectionErrorCount)
        assertTrue(store.state.sectionContent.listItems.isEmpty())
    }

    @Test
    fun openSectionOnFirstPageOtherErrorMarksErrorAndShowsUnknownMessage() = runTest(testDispatcher) {
        //Given
        var unknownErrorCount = 0
        val store = createStore(
            pages = mapOf(1 to CallResult.OtherError(throwable = Throwable())),
            onUnknownErrorSystemMessage = { unknownErrorCount++ }
        )

        //When
        store.accept(AnnouncedSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.ERROR }

        //Then
        assertEquals(1, unknownErrorCount)
        assertTrue(store.state.sectionContent.listItems.isEmpty())
    }

    @Test
    fun openSectionAfterARestorePagesInTheRestoredItemCount() = runTest(testDispatcher) {
        //Given
        val requestedPages = mutableListOf<Int>()
        val firstPageItems = listOf(testListItem(id = 1), testListItem(id = 2))
        val secondPageItems = listOf(testListItem(id = 3), testListItem(id = 4))
        val store = createStore(
            pages = mapOf(
                1 to CallResult.Success(firstPageItems),
                2 to CallResult.Success(secondPageItems)
            ),
            beforeAnnouncedResult = { page: Int -> requestedPages.add(page) }
        )
        store.accept(
            AnnouncedSectionStore.Intent.RestoreSection(
                itemCount = 3,
                enabledExtraEpisodesInfoIds = setOf(1)
            )
        )

        //When
        store.accept(AnnouncedSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //Then
        assertEquals(firstPageItems + secondPageItems, store.state.sectionContent.listItems)
        assertEquals(listOf(1, 2), requestedPages)
        assertEquals(setOf(1), store.state.sectionContent.enabledExtraEpisodesInfoIds)
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
            beforeAnnouncedResult = { page: Int -> requestedPages.add(page) }
        )
        store.accept(
            AnnouncedSectionStore.Intent.RestoreSection(
                itemCount = pageSize * pages.size,
                enabledExtraEpisodesInfoIds = setOf()
            )
        )

        //When
        store.accept(AnnouncedSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //Then
        assertEquals(listOf(1, 2, 3), requestedPages)
        assertEquals(pageSize * 3, store.state.sectionContent.listItems.size)
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
            AnnouncedSectionStore.Intent.RestoreSection(
                itemCount = 5,
                enabledExtraEpisodesInfoIds = setOf()
            )
        )

        //When
        store.accept(AnnouncedSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.ERROR }

        //Then
        assertEquals(1, connectionErrorCount)
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
            AnnouncedSectionStore.Intent.RestoreSection(
                itemCount = 5,
                enabledExtraEpisodesInfoIds = setOf()
            )
        )

        //When
        store.accept(AnnouncedSectionStore.Intent.OpenSection)
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
        store.accept(AnnouncedSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(AnnouncedSectionStore.Intent.LoadNextPage)
        pages[2] = CallResult.OtherError(throwable = Throwable())
        store.accept(AnnouncedSectionStore.Intent.LoadNextPage)

        //Then
        assertEquals(1, connectionErrorCount)
        assertEquals(1, unknownErrorCount)
        assertEquals(listOf(item), store.state.sectionContent.listItems)
        assertEquals(ContentTypeDomain.LOADED, store.state.sectionContent.contentType)
    }

    @Test
    fun openingAnAlreadyLoadedSectionLoadsNothingAgain() = runTest(testDispatcher) {
        //Given
        val requestedPages = mutableListOf<Int>()
        val store = createStore(
            pages = mapOf(1 to CallResult.Success(listOf(testListItem(id = 1)))),
            beforeAnnouncedResult = { page: Int -> requestedPages.add(page) }
        )
        store.accept(AnnouncedSectionStore.Intent.OpenSection)
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //When
        store.accept(AnnouncedSectionStore.Intent.OpenSection)

        //Then
        assertEquals(listOf(1), requestedPages)
    }

    @Test
    fun disposingTheStoreCancelsAnInFlightSectionLoad() = runTest(testDispatcher) {
        //Given
        var loadWasCancelled = false
        val store = createStore(
            pages = mapOf(),
            beforeAnnouncedResult = {
                try {
                    awaitCancellation()
                } finally {
                    loadWasCancelled = true
                }
            }
        )
        store.accept(AnnouncedSectionStore.Intent.OpenSection)

        //When
        store.dispose()

        //Then
        assertTrue(loadWasCancelled, "the load outlived its store")
    }
}
