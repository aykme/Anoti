package com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.store

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.ANIMATION_DURATION_SHORT
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase.wrapper.FavoritesUsecases
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderBase
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeFavoritesExecutorImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private object NoOpSource : AnimeFavoritesSource {
        override suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain> {
            error("not used in AnimeFavoritesExecutorImplTest")
        }
    }

    private object NoOpBackgroundUpdateUsecase : UpdateAllAnimeInBackgroundOnceUsecase {
        override fun execute() = Unit
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
        releaseStatus = ReleaseStatusDomain.ONGOING,
        episodesViewed = 0,
        isNewEpisode = false
    )

    private fun createStore(): AnimeFavoritesMainStore {
        val coroutineContextProvider = object : CoroutineContextProviderBase() {
            override val exceptionHandlerCallback: (Throwable) -> Unit = {}
        }
        val usecases = FavoritesUsecases(
            updateAllAnimeInBackgroundOnceUsecase = NoOpBackgroundUpdateUsecase,
            fetchAnimeDetailsByIdUsecase = FetchAnimeDetailsByIdUsecase(NoOpSource)
        )
        val toastProvider = ToastProvider(
            makeConnectionErrorToast = {},
            makeUnknownErrorToast = {}
        )
        val executorFactory: AnimeFavoritesExecutorFactory = {
            AnimeFavoritesExecutorImpl(
                coroutineContextProvider = coroutineContextProvider,
                usecases = usecases,
                toastProvider = toastProvider
            )
        }
        return AnimeFavoritesMainStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = executorFactory
        ).create()
    }

    @Test
    fun updateSectionImmediatelyShowsLoading() = runTest(testDispatcher) {
        val store = createStore()

        store.accept(AnimeFavoritesMainStore.Intent.UpdateSection)

        assertEquals(ContentTypeDomain.LOADING(isSwipeToRefresh = true), store.state.contentType)
    }

    @Test
    fun updateSectionKeepsLoadingUntilMinimumDurationElapsesEvenIfListArrivesSooner() = runTest(testDispatcher) {
        val store = createStore()
        val item = testListItem(id = 1)

        store.accept(AnimeFavoritesMainStore.Intent.UpdateSection)
        // Simulates the DB refresh landing (almost) immediately, followed by the screen's own
        // LaunchedEffect dispatching ItemsSubmittedToList as soon as it sees a non-empty list.
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        store.accept(AnimeFavoritesMainStore.Intent.ItemsSubmittedToList)

        assertEquals(ContentTypeDomain.LOADING(isSwipeToRefresh = true), store.state.contentType)

        advanceTimeBy(ANIMATION_DURATION_SHORT.inWholeMilliseconds / 2)
        runCurrent()
        assertEquals(ContentTypeDomain.LOADING(isSwipeToRefresh = true), store.state.contentType)

        advanceTimeBy(ANIMATION_DURATION_SHORT.inWholeMilliseconds)
        runCurrent()
        assertEquals(ContentTypeDomain.LOADED, store.state.contentType)
    }

    @Test
    fun updateSectionResolvesToEmptyWhenRefreshedListIsEmpty() = runTest(testDispatcher) {
        val store = createStore()

        store.accept(AnimeFavoritesMainStore.Intent.UpdateSection)
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(emptyList()))

        advanceTimeBy(ANIMATION_DURATION_SHORT.inWholeMilliseconds + 1)
        runCurrent()

        assertEquals(ContentTypeDomain.EMPTY, store.state.contentType)
    }

    @Test
    fun itemsSubmittedToListOutsideRefreshStillMarksLoaded() = runTest(testDispatcher) {
        val store = createStore()
        val item = testListItem(id = 1)

        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        store.accept(AnimeFavoritesMainStore.Intent.ItemsSubmittedToList)

        assertEquals(ContentTypeDomain.LOADED, store.state.contentType)
    }
}
