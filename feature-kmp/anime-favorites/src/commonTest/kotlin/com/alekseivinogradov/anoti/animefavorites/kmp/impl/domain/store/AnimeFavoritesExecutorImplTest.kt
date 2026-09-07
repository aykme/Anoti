package com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.store

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.ANIMATION_DURATION_SHORT
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase.wrapper.FavoritesUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderBase
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

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

    private class FakeDetailsSource(
        private val item: ListItemDomain
    ) : AnimeFavoritesSource {
        override suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain> {
            return CallResult.Success(item)
        }
    }

    private class TrackingCallSource(
        private val item: ListItemDomain
    ) : AnimeFavoritesSource {
        var callCount = 0
            private set
        val wasCalled: Boolean get() = callCount > 0

        override suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain> {
            callCount++
            return CallResult.Success(item)
        }
    }

    private object NoOpBackgroundUpdateUsecase : UpdateAllAnimeInBackgroundOnceUsecase {
        override fun execute() = Unit
    }

    private fun testListItem(
        id: AnimeId = 1,
        isExtraInfoEnabled: Boolean = false,
        nextEpisodeAt: String? = null
    ): ListItemDomain {
        return ListItemDomain(
            id = id,
            name = "Item $id",
            imageUrl = null,
            episodesAired = 1,
            episodesTotal = 12,
            nextEpisodeAt = nextEpisodeAt,
            airedOn = null,
            releasedOn = null,
            score = 8.0F,
            releaseStatus = ReleaseStatusDomain.ONGOING,
            episodesViewed = 0,
            isNewEpisode = false,
            isExtraInfoEnabled = isExtraInfoEnabled
        )
    }

    private fun createStore(
        source: AnimeFavoritesSource = NoOpSource
    ): AnimeFavoritesMainStore {
        val coroutineContextProvider = object : CoroutineContextProviderBase() {
            override val exceptionHandlerCallback: (Throwable) -> Unit = {}
        }
        val usecases = FavoritesUsecases(
            updateAllAnimeInBackgroundOnceUsecase = NoOpBackgroundUpdateUsecase,
            fetchAnimeDetailsByIdUsecase = FetchAnimeDetailsByIdUsecase(source)
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
        //Given
        val store = createStore()

        //When
        store.accept(AnimeFavoritesMainStore.Intent.UpdateSection)

        //Then
        assertEquals(ContentTypeDomain.LOADING(hasMinimumDuration = true), store.state.contentType)
    }

    @Test
    fun updateSectionKeepsLoadingUntilMinimumDurationElapsesEvenIfListArrivesSooner() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val item = testListItem()

        //When
        store.accept(AnimeFavoritesMainStore.Intent.UpdateSection)
        // Simulates the DB refresh landing (almost) immediately, followed by the screen's own
        // LaunchedEffect dispatching ItemsSubmittedToList as soon as it sees a non-empty list.
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        store.accept(AnimeFavoritesMainStore.Intent.ItemsSubmittedToList)

        //Then
        assertEquals(ContentTypeDomain.LOADING(hasMinimumDuration = true), store.state.contentType)

        //When
        advanceTimeBy((ANIMATION_DURATION_SHORT.inWholeMilliseconds / 2).milliseconds)
        runCurrent()

        //Then
        assertEquals(ContentTypeDomain.LOADING(hasMinimumDuration = true), store.state.contentType)

        //When
        advanceTimeBy(ANIMATION_DURATION_SHORT.inWholeMilliseconds.milliseconds)
        runCurrent()

        //Then
        assertEquals(ContentTypeDomain.LOADED, store.state.contentType)
    }

    @Test
    fun updateSectionKeepsLoadingPastMinimumDurationUntilListActuallyArrives() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val item = testListItem()

        //When
        store.accept(AnimeFavoritesMainStore.Intent.UpdateSection)
        // The database read is slower than the minimum duration this time.
        advanceTimeBy((ANIMATION_DURATION_SHORT.inWholeMilliseconds + 1).milliseconds)
        runCurrent()

        //Then
        assertEquals(ContentTypeDomain.LOADING(hasMinimumDuration = true), store.state.contentType)

        //When
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        runCurrent()

        //Then
        assertEquals(ContentTypeDomain.LOADED, store.state.contentType)
    }

    @Test
    fun updateSectionResolvesToEmptyWhenRefreshedListIsEmpty() = runTest(testDispatcher) {
        //Given
        val store = createStore()

        //When
        store.accept(AnimeFavoritesMainStore.Intent.UpdateSection)
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(emptyList()))
        advanceTimeBy((ANIMATION_DURATION_SHORT.inWholeMilliseconds + 1).milliseconds)
        runCurrent()

        //Then
        assertEquals(ContentTypeDomain.EMPTY, store.state.contentType)
    }

    @Test
    fun updateSectionPublishesResetExtraInfoAndUpdateSectionLabels() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val emittedLabels = mutableListOf<AnimeFavoritesMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeFavoritesMainStore.Intent.UpdateSection)

        //Then
        assertEquals(
            listOf(
                AnimeFavoritesMainStore.Label.ResetExtraInfo,
                AnimeFavoritesMainStore.Label.UpdateSection
            ),
            emittedLabels
        )
        collectJob.cancel()
    }

    @Test
    fun openSectionImmediatelyShowsLoadingWithMinimumDuration() = runTest(testDispatcher) {
        //Given
        val store = createStore()

        //When
        store.accept(AnimeFavoritesMainStore.Intent.OpenSection)

        //Then
        assertEquals(ContentTypeDomain.LOADING(hasMinimumDuration = true), store.state.contentType)
    }

    @Test
    fun openSectionPublishesNoLabels() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val emittedLabels = mutableListOf<AnimeFavoritesMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeFavoritesMainStore.Intent.OpenSection)

        //Then
        // The database reset for a section open is triggered directly by
        // NavAnimeFavoritesScreenComponent, not through a label here — see its own KDoc for why.
        assertTrue(emittedLabels.isEmpty(), "Expected no labels, got $emittedLabels")
        collectJob.cancel()
    }

    @Test
    fun itemsSubmittedToListOutsideRefreshStillMarksLoaded() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val item = testListItem()

        //When
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        store.accept(AnimeFavoritesMainStore.Intent.ItemsSubmittedToList)

        //Then
        assertEquals(ContentTypeDomain.LOADED, store.state.contentType)
    }

    @Test
    fun infoTypeClickOnMainItemPublishesUpdateListItemWithExtraInfoEnabled() = runTest(testDispatcher) {
        //Given
        val item = testListItem(nextEpisodeAt = "2026-09-10T12:00:00Z")
        val store = createStore()
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        val emittedLabels = mutableListOf<AnimeFavoritesMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeFavoritesMainStore.Intent.InfoTypeClick(id = item.id))

        //Then
        assertTrue(
            emittedLabels.contains(
                AnimeFavoritesMainStore.Label.UpdateListItem(
                    listItem = item.copy(isExtraInfoEnabled = true)
                )
            ),
            "Expected an UpdateListItem with isExtraInfoEnabled = true among $emittedLabels"
        )
        collectJob.cancel()
    }

    @Test
    fun infoTypeClickOnExtraItemPublishesUpdateListItemWithExtraInfoDisabled() = runTest(testDispatcher) {
        //Given
        val item = testListItem(isExtraInfoEnabled = true)
        val store = createStore()
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        val emittedLabels = mutableListOf<AnimeFavoritesMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeFavoritesMainStore.Intent.InfoTypeClick(id = item.id))

        //Then
        assertEquals(
            listOf<AnimeFavoritesMainStore.Label>(
                AnimeFavoritesMainStore.Label.UpdateListItem(
                    listItem = item.copy(isExtraInfoEnabled = false)
                )
            ),
            emittedLabels
        )
        collectJob.cancel()
    }

    @Test
    fun infoTypeClickToExtraWithoutNextEpisodeAtFetchesDetailsAndPublishesResult() = runTest(testDispatcher) {
        //Given
        val item = testListItem()
        val fetchedItem = item.copy(nextEpisodeAt = "2026-09-10T12:00:00Z")
        val store = createStore(source = FakeDetailsSource(fetchedItem))
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        val emittedLabels = mutableListOf<AnimeFavoritesMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeFavoritesMainStore.Intent.InfoTypeClick(id = item.id))

        //Then
        assertTrue(
            emittedLabels.contains(
                AnimeFavoritesMainStore.Label.UpdateListItem(
                    listItem = item.copy(nextEpisodeAt = fetchedItem.nextEpisodeAt)
                )
            ),
            "Expected the fetched nextEpisodeAt to be published among $emittedLabels"
        )
        collectJob.cancel()
    }

    @Test
    fun infoTypeClickToExtraWithNextEpisodeAtAlreadyKnownDoesNotFetchDetails() = runTest(testDispatcher) {
        //Given
        val item = testListItem(nextEpisodeAt = "2026-09-10T12:00:00Z")
        val source = TrackingCallSource(item)
        val store = createStore(source = source)
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))

        //When
        store.accept(AnimeFavoritesMainStore.Intent.InfoTypeClick(id = item.id))
        runCurrent()

        //Then
        assertEquals(false, source.wasCalled)
    }

    @Test
    fun infoTypeClickToExtraWithLegitimatelyNullNextEpisodeAtDoesNotRefetchOnLaterClick() = runTest(testDispatcher) {
        //Given
        val item = testListItem()
        // The API legitimately has no next-episode date: the fetch result keeps nextEpisodeAt
        // null, which must not be mistaken for "never fetched" on a later toggle.
        val source = TrackingCallSource(item)
        val store = createStore(source = source)
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))

        //When
        store.accept(AnimeFavoritesMainStore.Intent.InfoTypeClick(id = item.id))
        runCurrent()
        store.accept(AnimeFavoritesMainStore.Intent.InfoTypeClick(id = item.id))
        runCurrent()

        //Then
        assertEquals(1, source.callCount)
    }

    @Test
    fun openSectionResetsFetchedAnimeDetailsIdsSoARefreshedNullResultIsRefetched() = runTest(testDispatcher) {
        //Given
        val item = testListItem()
        val source = TrackingCallSource(item)
        val store = createStore(source = source)
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        store.accept(AnimeFavoritesMainStore.Intent.InfoTypeClick(id = item.id))
        runCurrent()

        //When
        store.accept(AnimeFavoritesMainStore.Intent.OpenSection)
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        store.accept(AnimeFavoritesMainStore.Intent.InfoTypeClick(id = item.id))
        runCurrent()

        //Then
        assertEquals(2, source.callCount)
    }
}
