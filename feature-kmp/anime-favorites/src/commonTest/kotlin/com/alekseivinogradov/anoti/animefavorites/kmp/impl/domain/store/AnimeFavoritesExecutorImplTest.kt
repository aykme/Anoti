package com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.store

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.usecase.fake.UpdateAllAnimeInBackgroundOnceUsecaseFake
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.ANIMATION_DURATION_SHORT
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.LIST_ARRIVAL_TIMEOUT_SECONDS
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.data.source.fake.AnimeFavoritesSourceFake
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase.wrapper.FavoritesUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.fake.CoroutineContextProviderFake
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
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
        source: AnimeFavoritesSource = AnimeFavoritesSourceFake(),
        backgroundUpdateUsecase: UpdateAllAnimeInBackgroundOnceUsecase = UpdateAllAnimeInBackgroundOnceUsecaseFake(),
        onConnectionErrorSystemMessage: () -> Unit = {},
        onUnknownErrorSystemMessage: () -> Unit = {}
    ): AnimeFavoritesMainStore {
        val coroutineContextProvider = CoroutineContextProviderFake()
        val usecases = FavoritesUsecases(
            updateAllAnimeInBackgroundOnceUsecase = backgroundUpdateUsecase,
            fetchAnimeDetailsByIdUsecase = FetchAnimeDetailsByIdUsecase(source)
        )
        val systemMessageProvider = SystemMessageProvider(
            makeConnectionErrorSystemMessage = onConnectionErrorSystemMessage,
            makeUnknownErrorSystemMessage = onUnknownErrorSystemMessage
        )
        val executorFactory: AnimeFavoritesExecutorFactory = {
            AnimeFavoritesExecutorImpl(
                coroutineContextProvider = coroutineContextProvider,
                usecases = usecases,
                systemMessageProvider = systemMessageProvider
            )
        }
        return AnimeFavoritesMainStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = executorFactory
        ).create().also(createdStores::add)
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
        val store = createStore(
            source = AnimeFavoritesSourceFake { _, _ -> CallResult.Success(fetchedItem) }
        )
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
        val source = AnimeFavoritesSourceFake { _, _ -> CallResult.Success(item) }
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
        val source = AnimeFavoritesSourceFake { _, _ -> CallResult.Success(item) }
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
        val source = AnimeFavoritesSourceFake { _, _ -> CallResult.Success(item) }
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

    @Test
    fun aRefreshWhoseListNeverArrivesStopsShowingLoading() = runTest(testDispatcher) {
        //Given
        val store = createStore()

        //When
        // Nothing dispatches UpdateListItems: the database answered no write, which is what a
        // refresh that changes no row looks like once the repeated publishing is gone.
        store.accept(AnimeFavoritesMainStore.Intent.UpdateSection)
        advanceTimeBy(
            ANIMATION_DURATION_SHORT + LIST_ARRIVAL_TIMEOUT_SECONDS + 1L.milliseconds
        )
        runCurrent()

        //Then
        assertEquals(ContentTypeDomain.EMPTY, store.state.contentType)
    }

    @Test
    fun aSecondDetailsFetchForTheSameItemReplacesTheFirst() = runTest(testDispatcher) {
        //Given
        val item = testListItem()
        val fetched = item.copy(nextEpisodeAt = "2026-09-10T12:00:00Z")
        val source = AnimeFavoritesSourceFake { _, callNumber ->
            if (callNumber == 1) awaitCancellation() else CallResult.Success(fetched)
        }
        val store = createStore(source = source)
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        store.accept(AnimeFavoritesMainStore.Intent.InfoTypeClick(id = item.id))
        val emittedLabels = mutableListOf<AnimeFavoritesMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeFavoritesMainStore.Intent.InfoTypeClick(id = item.id))
        runCurrent()

        //Then
        assertEquals(listOf(1), source.canceledCalls, "the replaced fetch outlived its replacement")
        assertTrue(
            emittedLabels.contains(
                AnimeFavoritesMainStore.Label.UpdateListItem(
                    listItem = item.copy(nextEpisodeAt = fetched.nextEpisodeAt)
                )
            ),
            "Expected the replacement fetch's result to be published among $emittedLabels"
        )
        collectJob.cancel()
    }

    @Test
    fun disposingTheStoreCancelsAnInFlightDetailsFetch() = runTest(testDispatcher) {
        //Given
        val source = AnimeFavoritesSourceFake { _, _ -> awaitCancellation() }
        val item = testListItem()
        val store = createStore(source = source)
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        store.accept(AnimeFavoritesMainStore.Intent.InfoTypeClick(id = item.id))

        //When
        store.dispose()

        //Then
        assertTrue(source.wasCanceled, "the details fetch outlived its store")
    }

    @Test
    fun anEmptyListArrivingOutsideARefreshShowsLoadingThenEmpty() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(testListItem())))
        store.accept(AnimeFavoritesMainStore.Intent.ItemsSubmittedToList)

        //When
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(emptyList()))

        //Then
        assertEquals(ContentTypeDomain.LOADING(), store.state.contentType)

        //When
        advanceTimeBy((ANIMATION_DURATION_SHORT.inWholeMilliseconds + 1).milliseconds)
        runCurrent()

        //Then
        assertEquals(ContentTypeDomain.EMPTY, store.state.contentType)
    }

    @Test
    fun anEmptyListArrivingOnAnAlreadyEmptyListDoesNotShowLoadingAgain() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(emptyList()))
        advanceTimeBy((ANIMATION_DURATION_SHORT.inWholeMilliseconds + 1).milliseconds)
        runCurrent()

        //When
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(emptyList()))

        //Then
        assertEquals(ContentTypeDomain.EMPTY, store.state.contentType)
    }

    @Test
    fun anEmptyListArrivingWhileARefreshResolvesIsLeftToThatRefresh() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        store.accept(AnimeFavoritesMainStore.Intent.UpdateSection)

        //When
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(emptyList()))

        //Then
        assertEquals(ContentTypeDomain.LOADING(hasMinimumDuration = true), store.state.contentType)
    }

    @Test
    fun aLaterListArrivalCancelsTheEmptyTransitionOfAnEarlierOne() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val item = testListItem()
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        store.accept(AnimeFavoritesMainStore.Intent.ItemsSubmittedToList)
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(emptyList()))

        //When
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        store.accept(AnimeFavoritesMainStore.Intent.ItemsSubmittedToList)
        advanceTimeBy((ANIMATION_DURATION_SHORT.inWholeMilliseconds + 1).milliseconds)
        runCurrent()

        //Then
        assertEquals(ContentTypeDomain.LOADED, store.state.contentType)
    }

    @Test
    fun openSectionCancelsAPendingEmptyListTransition() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val item = testListItem()
        val halfDuration = (ANIMATION_DURATION_SHORT.inWholeMilliseconds / 2).milliseconds
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        store.accept(AnimeFavoritesMainStore.Intent.ItemsSubmittedToList)
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(emptyList()))
        advanceTimeBy(halfDuration)
        runCurrent()

        //When
        store.accept(AnimeFavoritesMainStore.Intent.OpenSection)
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        // Past the moment the canceled transition would have fired, short of the open's own.
        advanceTimeBy(halfDuration + 1L.milliseconds)
        runCurrent()

        //Then
        assertEquals(ContentTypeDomain.LOADING(hasMinimumDuration = true), store.state.contentType)
    }

    @Test
    fun updateSectionCancelsAPendingEmptyListTransition() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val item = testListItem()
        val halfDuration = (ANIMATION_DURATION_SHORT.inWholeMilliseconds / 2).milliseconds
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        store.accept(AnimeFavoritesMainStore.Intent.ItemsSubmittedToList)
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(emptyList()))
        advanceTimeBy(halfDuration)
        runCurrent()

        //When
        store.accept(AnimeFavoritesMainStore.Intent.UpdateSection)
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        // Past the moment the canceled transition would have fired, short of the refresh's own.
        advanceTimeBy(halfDuration + 1L.milliseconds)
        runCurrent()

        //Then
        assertEquals(ContentTypeDomain.LOADING(hasMinimumDuration = true), store.state.contentType)
    }

    @Test
    fun itemClickPublishesItemClickLabel() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val emittedLabels = mutableListOf<AnimeFavoritesMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeFavoritesMainStore.Intent.ItemClick(id = 7))

        //Then
        assertEquals(
            listOf<AnimeFavoritesMainStore.Label>(
                AnimeFavoritesMainStore.Label.ItemClick(id = 7)
            ),
            emittedLabels
        )
        collectJob.cancel()
    }

    @Test
    fun notificationClickPublishesDisableNotificationClickLabel() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val emittedLabels = mutableListOf<AnimeFavoritesMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeFavoritesMainStore.Intent.NotificationClick(id = 7))

        //Then
        assertEquals(
            listOf<AnimeFavoritesMainStore.Label>(
                AnimeFavoritesMainStore.Label.DisableNotificationClick(id = 7)
            ),
            emittedLabels
        )
        collectJob.cancel()
    }

    @Test
    fun episodesViewedMinusClickPublishesADecrementedItem() = runTest(testDispatcher) {
        //Given
        val item = testListItem().copy(episodesViewed = 3)
        val store = createStore()
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        val emittedLabels = mutableListOf<AnimeFavoritesMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeFavoritesMainStore.Intent.EpisodesViewedMinusClick(id = item.id))

        //Then
        assertEquals(
            listOf<AnimeFavoritesMainStore.Label>(
                AnimeFavoritesMainStore.Label.UpdateListItem(listItem = item.copy(episodesViewed = 2))
            ),
            emittedLabels
        )
        collectJob.cancel()
    }

    @Test
    fun episodesViewedMinusClickOnAnUnwatchedItemPublishesNothing() = runTest(testDispatcher) {
        //Given
        val item = testListItem()
        val store = createStore()
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        val emittedLabels = mutableListOf<AnimeFavoritesMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeFavoritesMainStore.Intent.EpisodesViewedMinusClick(id = item.id))

        //Then
        assertTrue(emittedLabels.isEmpty(), "Expected no labels, got $emittedLabels")
        collectJob.cancel()
    }

    @Test
    fun episodesViewedMinusClickForAnUnknownIdPublishesNothing() = runTest(testDispatcher) {
        //Given
        val item = testListItem(id = 1).copy(episodesViewed = 3)
        val store = createStore()
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        val emittedLabels = mutableListOf<AnimeFavoritesMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeFavoritesMainStore.Intent.EpisodesViewedMinusClick(id = 2))

        //Then
        assertTrue(emittedLabels.isEmpty(), "Expected no labels, got $emittedLabels")
        collectJob.cancel()
    }

    @Test
    fun episodesViewedPlusClickPublishesAnIncrementedItem() = runTest(testDispatcher) {
        //Given
        val item = testListItem().copy(episodesAired = 5, episodesViewed = 2)
        val store = createStore()
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        val emittedLabels = mutableListOf<AnimeFavoritesMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeFavoritesMainStore.Intent.EpisodesViewedPlusClick(id = item.id))

        //Then
        assertEquals(
            listOf<AnimeFavoritesMainStore.Label>(
                AnimeFavoritesMainStore.Label.UpdateListItem(listItem = item.copy(episodesViewed = 3))
            ),
            emittedLabels
        )
        collectJob.cancel()
    }

    @Test
    fun episodesViewedPlusClickOnAnOngoingItemStopsAtEpisodesAired() = runTest(testDispatcher) {
        //Given
        val item = testListItem().copy(
            episodesAired = 5,
            episodesTotal = 12,
            episodesViewed = 5,
            releaseStatus = ReleaseStatusDomain.ONGOING
        )
        val store = createStore()
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        val emittedLabels = mutableListOf<AnimeFavoritesMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeFavoritesMainStore.Intent.EpisodesViewedPlusClick(id = item.id))

        //Then
        assertTrue(emittedLabels.isEmpty(), "Expected no labels, got $emittedLabels")
        collectJob.cancel()
    }

    @Test
    fun episodesViewedPlusClickOnAReleasedItemStopsAtEpisodesTotal() = runTest(testDispatcher) {
        //Given
        val item = testListItem().copy(
            episodesAired = 5,
            episodesTotal = 12,
            episodesViewed = 12,
            releaseStatus = ReleaseStatusDomain.RELEASED
        )
        val store = createStore()
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        val emittedLabels = mutableListOf<AnimeFavoritesMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeFavoritesMainStore.Intent.EpisodesViewedPlusClick(id = item.id))

        //Then
        assertTrue(emittedLabels.isEmpty(), "Expected no labels, got $emittedLabels")
        collectJob.cancel()
    }

    @Test
    fun episodesViewedPlusClickOnAnAnnouncedItemPublishesNothing() = runTest(testDispatcher) {
        //Given
        val item = testListItem().copy(
            episodesAired = 5,
            releaseStatus = ReleaseStatusDomain.ANNOUNCED
        )
        val store = createStore()
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        val emittedLabels = mutableListOf<AnimeFavoritesMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeFavoritesMainStore.Intent.EpisodesViewedPlusClick(id = item.id))

        //Then
        assertTrue(emittedLabels.isEmpty(), "Expected no labels, got $emittedLabels")
        collectJob.cancel()
    }

    @Test
    fun episodesViewedPlusClickOnAnItemOfUnknownStatusPublishesNothing() = runTest(testDispatcher) {
        //Given
        val item = testListItem().copy(
            episodesAired = 5,
            releaseStatus = ReleaseStatusDomain.UNKNOWN
        )
        val store = createStore()
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        val emittedLabels = mutableListOf<AnimeFavoritesMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeFavoritesMainStore.Intent.EpisodesViewedPlusClick(id = item.id))

        //Then
        assertTrue(emittedLabels.isEmpty(), "Expected no labels, got $emittedLabels")
        collectJob.cancel()
    }

    @Test
    fun episodesViewedPlusClickWithAnUnknownEpisodeCountPublishesNothing() = runTest(testDispatcher) {
        //Given
        val item = testListItem().copy(episodesAired = null)
        val store = createStore()
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        val emittedLabels = mutableListOf<AnimeFavoritesMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeFavoritesMainStore.Intent.EpisodesViewedPlusClick(id = item.id))

        //Then
        assertTrue(emittedLabels.isEmpty(), "Expected no labels, got $emittedLabels")
        collectJob.cancel()
    }

    @Test
    fun episodesViewedPlusClickForAnUnknownIdPublishesNothing() = runTest(testDispatcher) {
        //Given
        val item = testListItem(id = 1).copy(episodesAired = 5)
        val store = createStore()
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        val emittedLabels = mutableListOf<AnimeFavoritesMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeFavoritesMainStore.Intent.EpisodesViewedPlusClick(id = 2))

        //Then
        assertTrue(emittedLabels.isEmpty(), "Expected no labels, got $emittedLabels")
        collectJob.cancel()
    }

    @Test
    fun updateAllItemsInBackgroundTriggersTheBackgroundUpdate() = runTest(testDispatcher) {
        //Given
        val backgroundUpdateUsecase = UpdateAllAnimeInBackgroundOnceUsecaseFake()
        val store = createStore(backgroundUpdateUsecase = backgroundUpdateUsecase)

        //When
        store.accept(AnimeFavoritesMainStore.Intent.UpdateAllItemsInBackground)

        //Then
        assertEquals(1, backgroundUpdateUsecase.executeCount)
    }

    @Test
    fun aDetailsFetchRejectedByTheServerShowsTheConnectionMessage() = runTest(testDispatcher) {
        //Given
        var connectionErrorCount = 0
        var unknownErrorCount = 0
        val item = testListItem()
        val store = createStore(
            source = AnimeFavoritesSourceFake { _, _ -> CallResult.HttpError(code = 500, throwable = Throwable()) },
            onConnectionErrorSystemMessage = { connectionErrorCount++ },
            onUnknownErrorSystemMessage = { unknownErrorCount++ }
        )
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))

        //When
        store.accept(AnimeFavoritesMainStore.Intent.InfoTypeClick(id = item.id))
        runCurrent()

        //Then
        assertEquals(1, connectionErrorCount)
        assertEquals(0, unknownErrorCount)
    }

    @Test
    fun aDetailsFetchThatNeverReachedTheServerShowsTheConnectionMessage() = runTest(testDispatcher) {
        //Given
        var connectionErrorCount = 0
        var unknownErrorCount = 0
        val item = testListItem()
        val store = createStore(
            source = AnimeFavoritesSourceFake { _, _ -> CallResult.NetworkError(throwable = Throwable()) },
            onConnectionErrorSystemMessage = { connectionErrorCount++ },
            onUnknownErrorSystemMessage = { unknownErrorCount++ }
        )
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))

        //When
        store.accept(AnimeFavoritesMainStore.Intent.InfoTypeClick(id = item.id))
        runCurrent()

        //Then
        assertEquals(1, connectionErrorCount)
        assertEquals(0, unknownErrorCount)
    }

    @Test
    fun aDetailsFetchFailingForAnotherReasonShowsTheUnknownMessage() = runTest(testDispatcher) {
        //Given
        var connectionErrorCount = 0
        var unknownErrorCount = 0
        val item = testListItem()
        val store = createStore(
            source = AnimeFavoritesSourceFake { _, _ -> CallResult.OtherError(throwable = Throwable()) },
            onConnectionErrorSystemMessage = { connectionErrorCount++ },
            onUnknownErrorSystemMessage = { unknownErrorCount++ }
        )
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))

        //When
        store.accept(AnimeFavoritesMainStore.Intent.InfoTypeClick(id = item.id))
        runCurrent()

        //Then
        assertEquals(1, unknownErrorCount)
        assertEquals(0, connectionErrorCount)
    }

    @Test
    fun detailsArrivingForAnItemNoLongerInTheListAreDropped() = runTest(testDispatcher) {
        //Given
        val item = testListItem()
        val fetched = item.copy(nextEpisodeAt = "2026-09-10T12:00:00Z")
        val gate = CompletableDeferred<Unit>()
        val source = AnimeFavoritesSourceFake { _, _ ->
            gate.await()
            CallResult.Success(fetched)
        }
        val store = createStore(source = source)
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(listOf(item)))
        store.accept(AnimeFavoritesMainStore.Intent.InfoTypeClick(id = item.id))
        val emittedLabels = mutableListOf<AnimeFavoritesMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeFavoritesMainStore.Intent.UpdateListItems(emptyList()))
        gate.complete(Unit)
        runCurrent()

        //Then
        assertTrue(
            store.state.fetchedAnimeDetailsIds.isEmpty(),
            "a removed item was marked as fetched"
        )
        assertTrue(emittedLabels.isEmpty(), "Expected no labels, got $emittedLabels")
        collectJob.cancel()
    }
}
