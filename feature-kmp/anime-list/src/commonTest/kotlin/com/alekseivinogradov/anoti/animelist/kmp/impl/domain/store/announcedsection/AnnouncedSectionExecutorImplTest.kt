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
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderBase
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

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
        private val beforeAnnouncedResult: suspend () -> Unit = {}
    ) : AnimeListSource {
        override suspend fun getOngoingList(page: Int, sort: SortData): CallResult<List<ListItemDomain>> {
            error("not used in AnnouncedSectionExecutorImplTest")
        }

        override suspend fun getAnnouncedList(page: Int, sort: SortData): CallResult<List<ListItemDomain>> {
            beforeAnnouncedResult()
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
        beforeAnnouncedResult: suspend () -> Unit = {},
        onConnectionErrorToast: () -> Unit = {}
    ): AnnouncedSectionStore {
        val source = FakeAnnouncedSource(pages, beforeAnnouncedResult)
        val coroutineContextProvider = object : CoroutineContextProviderBase() {
            override val exceptionHandlerCallback: (Throwable) -> Unit = {}
        }
        val usecases = AnnouncedUsecases(
            fetchAnnouncedAnimeListUsecase = FetchAnnouncedAnimeListUsecase(source)
        )
        val toastProvider = ToastProvider(
            makeConnectionErrorToast = onConnectionErrorToast,
            makeUnknownErrorToast = {}
        )
        val executorFactory: AnnouncedSectionExecutorFactory = {
            AnnouncedSectionExecutorImpl(
                coroutineContextProvider = coroutineContextProvider,
                usecases = usecases,
                toastProvider = toastProvider
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
        assertTrue(store.state.sectionContent.enabledExtraEpisodesInfoIds.contains(item.id))
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
