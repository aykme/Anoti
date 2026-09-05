package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.navigation

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionHatDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.announcedsection.AnnouncedSectionExecutorFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.announcedsection.AnnouncedSectionExecutorImpl
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.announcedsection.AnnouncedSectionStoreFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.main.AnimeListExecutorFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.main.AnimeListExecutorImpl
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.main.AnimeListMainStoreFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.searchsection.SearchSectionExecutorFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.searchsection.SearchSectionExecutorImpl
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.searchsection.SearchSectionStoreFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnimeListBySearchUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnnouncedAnimeListUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper.AnnouncedUsecases
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

    private fun createAnnouncedStore(): AnnouncedSectionStore {
        val usecases = AnnouncedUsecases(
            fetchAnnouncedAnimeListUsecase = FetchAnnouncedAnimeListUsecase(EmptySource)
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

    private fun createSearchStore(): SearchSectionStore {
        val usecases = SearchUsecases(
            fetchAnimeListBySearchUsecase = FetchAnimeListBySearchUsecase(EmptySource),
            fetchAnimeDetailsByIdUsecase = FetchAnimeDetailsByIdUsecase(EmptySource)
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
    fun nullRestoredStateLeavesOngoingSelected() = runTest(testDispatcher) {
        //Given
        val mainStore = createMainStore()
        val announcedStore = createAnnouncedStore()
        val searchStore = createSearchStore()

        //When
        applyRestoredMainState(
            restoredState = null,
            mainStore = mainStore,
            announcedSectionStore = announcedStore,
            searchSectionStore = searchStore
        )

        //Then
        assertEquals(SectionHatDomain.ONGOINGS, mainStore.state.selectedSection)
        assertEquals(ContentTypeDomain.LOADING, announcedStore.state.sectionContent.contentType)
    }

    @Test
    fun ongoingsRestoredStateLeavesOngoingSelected() = runTest(testDispatcher) {
        //Given
        val mainStore = createMainStore()
        val announcedStore = createAnnouncedStore()
        val searchStore = createSearchStore()

        //When
        applyRestoredMainState(
            restoredState = RestoredMainState(SectionHatDomain.ONGOINGS, searchText = ""),
            mainStore = mainStore,
            announcedSectionStore = announcedStore,
            searchSectionStore = searchStore
        )

        //Then
        assertEquals(SectionHatDomain.ONGOINGS, mainStore.state.selectedSection)
        assertEquals(ContentTypeDomain.LOADING, announcedStore.state.sectionContent.contentType)
    }

    @Test
    fun announcedRestoredStateSelectsSectionAndOpensAnnouncedStoreDirectly() = runTest(testDispatcher) {
        //Given
        val mainStore = createMainStore()
        val announcedStore = createAnnouncedStore()
        val searchStore = createSearchStore()

        //When
        applyRestoredMainState(
            restoredState = RestoredMainState(SectionHatDomain.ANNOUNCED, searchText = ""),
            mainStore = mainStore,
            announcedSectionStore = announcedStore,
            searchSectionStore = searchStore
        )
        announcedStore.states.first { it.sectionContent.contentType == ContentTypeDomain.LOADED }

        //Then
        assertEquals(SectionHatDomain.ANNOUNCED, mainStore.state.selectedSection)
        assertEquals(ContentTypeDomain.LOADED, announcedStore.state.sectionContent.contentType)
        assertEquals(true, mainStore.state.isNeedToResetListPositon)
    }

    @Test
    fun searchRestoredStateWithTextDispatchesTextDirectlyToSearchStore() = runTest(testDispatcher) {
        //Given
        val mainStore = createMainStore()
        val announcedStore = createAnnouncedStore()
        val searchStore = createSearchStore()

        //When
        applyRestoredMainState(
            restoredState = RestoredMainState(SectionHatDomain.SEARCH, searchText = "totoro"),
            mainStore = mainStore,
            announcedSectionStore = announcedStore,
            searchSectionStore = searchStore
        )

        //Then
        assertEquals(SectionHatDomain.SEARCH, mainStore.state.selectedSection)
        assertEquals("totoro", mainStore.state.search.searchText)
        assertEquals("totoro", searchStore.state.searchText)
        assertEquals(true, mainStore.state.isNeedToResetListPositon)
    }

    @Test
    fun searchRestoredStateWithBlankTextOpensSectionWithoutChangingSearchText() = runTest(testDispatcher) {
        //Given
        val mainStore = createMainStore()
        val announcedStore = createAnnouncedStore()
        val searchStore = createSearchStore()

        //When
        applyRestoredMainState(
            restoredState = RestoredMainState(SectionHatDomain.SEARCH, searchText = ""),
            mainStore = mainStore,
            announcedSectionStore = announcedStore,
            searchSectionStore = searchStore
        )

        //Then
        assertEquals(SectionHatDomain.SEARCH, mainStore.state.selectedSection)
        assertEquals("", searchStore.state.searchText)
        assertEquals(true, mainStore.state.isNeedToResetListPositon)
    }
}
