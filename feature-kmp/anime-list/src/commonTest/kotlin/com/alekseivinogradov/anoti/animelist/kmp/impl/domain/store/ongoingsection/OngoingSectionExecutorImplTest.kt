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
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderBase
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class OngoingSectionExecutorImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeOngoingSource(
        private val pages: Map<Int, CallResult<List<ListItemDomain>>>
    ) : AnimeListSource {
        override suspend fun getOngoingList(page: Int, sort: SortData): CallResult<List<ListItemDomain>> {
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

        override suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain> {
            error("not used in OngoingSectionExecutorImplTest")
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

    private fun createStore(
        pages: Map<Int, CallResult<List<ListItemDomain>>>,
        onConnectionErrorToast: () -> Unit = {},
        onUnknownErrorToast: () -> Unit = {}
    ): OngoingSectionStore {
        val source = FakeOngoingSource(pages)
        val coroutineContextProvider = object : CoroutineContextProviderBase() {
            override val exceptionHandlerCallback: (Throwable) -> Unit = {}
        }
        val usecases = OngoingUsecases(
            fetchOngoingAnimeListUsecase = FetchOngoingAnimeListUsecase(source),
            fetchAnimeDetailsByIdUsecase = FetchAnimeDetailsByIdUsecase(source)
        )
        val toastProvider = ToastProvider(
            makeConnectionErrorToast = onConnectionErrorToast,
            makeUnknownErrorToast = onUnknownErrorToast
        )
        val executorFactory: OngoingSectionExecutorFactory = {
            OngoingSectionExecutorImpl(
                coroutineContextProvider = coroutineContextProvider,
                usecases = usecases,
                toastProvider = toastProvider
            )
        }
        return OngoingSectionStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = executorFactory
        ).create()
    }

    @Test
    fun openSectionLoadsFirstPageAndMarksLoaded() = runTest(testDispatcher) {
        val item = testListItem(id = 1)
        val store = createStore(pages = mapOf(1 to CallResult.Success(listOf(item))))

        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        assertEquals(listOf(item), store.state.sectionContent.listItems)
    }

    @Test
    fun openSectionOnFirstPageHttpErrorMarksErrorAndToasts() = runTest(testDispatcher) {
        var toastCount = 0
        val store = createStore(
            pages = mapOf(1 to CallResult.HttpError(code = 500, throwable = Throwable())),
            onConnectionErrorToast = { toastCount++ }
        )

        store.states.first { it.sectionContent.contentType == ContentTypeDomain.ERROR }

        assertEquals(1, toastCount)
        assertTrue(store.state.sectionContent.listItems.isEmpty())
    }

    @Test
    fun loadNextPageAppendsSecondPageItems() = runTest(testDispatcher) {
        val firstItem = testListItem(id = 1)
        val secondItem = testListItem(id = 2)
        val store = createStore(
            pages = mapOf(
                1 to CallResult.Success(listOf(firstItem)),
                2 to CallResult.Success(listOf(secondItem))
            )
        )
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        store.accept(OngoingSectionStore.Intent.LoadNextPage)
        store.states.first { it.sectionContent.listItems.size == 2 }

        assertEquals(listOf(firstItem, secondItem), store.state.sectionContent.listItems)
    }

    @Test
    fun loadNextPageOnHttpErrorLeavesListAndContentTypeUnchanged() = runTest(testDispatcher) {
        var toastCount = 0
        val item = testListItem(id = 1)
        val store = createStore(
            pages = mapOf(
                1 to CallResult.Success(listOf(item)),
                2 to CallResult.HttpError(code = 500, throwable = Throwable())
            ),
            onConnectionErrorToast = { toastCount++ }
        )
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        store.accept(OngoingSectionStore.Intent.LoadNextPage)
        store.states.first { toastCount == 1 }

        assertEquals(listOf(item), store.state.sectionContent.listItems)
        assertEquals(ContentTypeDomain.LOADED, store.state.sectionContent.contentType)
    }

    @Test
    fun loadNextPageAtEndOfListDoesNothing() = runTest(testDispatcher) {
        val item = testListItem(id = 1)
        val store = createStore(
            pages = mapOf(
                1 to CallResult.Success(listOf(item)),
                2 to CallResult.Success(emptyList())
            )
        )
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }
        store.accept(OngoingSectionStore.Intent.LoadNextPage)
        store.states.first { it.sectionContent.listItems.size == 1 }

        store.accept(OngoingSectionStore.Intent.LoadNextPage)

        assertEquals(listOf(item), store.state.sectionContent.listItems)
    }

    @Test
    fun episodesInfoClickResolvesItemByIdAndTogglesEnabledSet() = runTest(testDispatcher) {
        val item = testListItem(id = 1)
        val store = createStore(pages = mapOf(1 to CallResult.Success(listOf(item))))
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        store.accept(OngoingSectionStore.Intent.EpisodesInfoClick(id = item.id))
        assertEquals(setOf(item.id), store.state.sectionContent.enabledExtraEpisodesInfoIds)

        store.accept(OngoingSectionStore.Intent.EpisodesInfoClick(id = item.id))
        assertEquals(emptySet(), store.state.sectionContent.enabledExtraEpisodesInfoIds)
    }

    @Test
    fun episodesInfoClickWithUnknownIdIsNoOp() = runTest(testDispatcher) {
        val item = testListItem(id = 1)
        val store = createStore(pages = mapOf(1 to CallResult.Success(listOf(item))))
        store.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        store.accept(OngoingSectionStore.Intent.EpisodesInfoClick(id = 999))

        assertTrue(store.state.sectionContent.enabledExtraEpisodesInfoIds.isEmpty())
    }
}
