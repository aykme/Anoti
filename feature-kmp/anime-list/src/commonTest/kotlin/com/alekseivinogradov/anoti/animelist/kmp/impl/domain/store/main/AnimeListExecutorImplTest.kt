package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.main

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.ANIMATION_DURATION_VERY_SHORT
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.AnimeDetails
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SearchDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionContentDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionHatDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderBase
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.fake.CoroutineContextProviderFake
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.coroutines.CoroutineContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeListExecutorImplTest {

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

    private fun createStore(
        coroutineContextProvider: CoroutineContextProvider = CoroutineContextProviderFake()
    ): AnimeListMainStore {
        val executorFactory: AnimeListExecutorFactory = {
            AnimeListExecutorImpl(coroutineContextProvider = coroutineContextProvider)
        }
        return AnimeListMainStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = executorFactory
        ).create().also(createdStores::add)
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

    @Test
    fun notificationClickOnKnownIdInSelectedSectionPublishesEnableWithResolvedItem() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val item = testListItem(id = 1)
        store.accept(
            AnimeListMainStore.Intent.UpdateOngoingContent(
                content = SectionContentDomain(listItems = listOf(item))
            )
        )
        val emittedLabels = mutableListOf<AnimeListMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeListMainStore.Intent.NotificationClick(id = item.id))

        //Then
        assertTrue(
            emittedLabels.contains(AnimeListMainStore.Label.EnableNotificationClick(item)),
            "Expected EnableNotificationClick($item) among $emittedLabels"
        )
        collectJob.cancel()
    }

    @Test
    fun notificationClickOnAlreadyEnabledIdPublishesDisableWithId() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val item = testListItem(id = 2)
        store.accept(
            AnimeListMainStore.Intent.UpdateOngoingContent(
                content = SectionContentDomain(listItems = listOf(item))
            )
        )
        store.accept(
            AnimeListMainStore.Intent.UpdateEnabledNotificationIds(
                enabledNotificationIds = setOf(item.id)
            )
        )
        val emittedLabels = mutableListOf<AnimeListMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeListMainStore.Intent.NotificationClick(id = item.id))

        //Then
        assertEquals(
            listOf<AnimeListMainStore.Label>(AnimeListMainStore.Label.DisableNotificationClick(item.id)),
            emittedLabels
        )
        collectJob.cancel()
    }

    @Test
    fun notificationClickOnUnknownIdPublishesNothing() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        store.accept(
            AnimeListMainStore.Intent.UpdateOngoingContent(
                content = SectionContentDomain(listItems = listOf(testListItem(id = 1)))
            )
        )
        val emittedLabels = mutableListOf<AnimeListMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeListMainStore.Intent.NotificationClick(id = 999))

        //Then
        assertTrue(emittedLabels.isEmpty(), "Expected no labels, got $emittedLabels")
        collectJob.cancel()
    }

    @Test
    fun notificationClickResolvesTheItemFromTheSelectedSection() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val announcedItem = testListItem(id = 2)
        val searchItem = testListItem(id = 3)
        store.accept(
            AnimeListMainStore.Intent.UpdateOngoingContent(
                content = SectionContentDomain(listItems = listOf(testListItem(id = 1)))
            )
        )
        store.accept(
            AnimeListMainStore.Intent.UpdateAnnouncedContent(
                content = SectionContentDomain(listItems = listOf(announcedItem))
            )
        )
        store.accept(
            AnimeListMainStore.Intent.UpdateSearchContent(
                content = SectionContentDomain(listItems = listOf(searchItem))
            )
        )
        val emittedLabels = mutableListOf<AnimeListMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeListMainStore.Intent.AnnouncedSectionClick)
        store.accept(AnimeListMainStore.Intent.NotificationClick(id = announcedItem.id))
        store.accept(AnimeListMainStore.Intent.SearchSectionClick)
        store.accept(AnimeListMainStore.Intent.NotificationClick(id = searchItem.id))

        //Then
        assertTrue(
            emittedLabels.containsAll(
                listOf(
                    AnimeListMainStore.Label.EnableNotificationClick(announcedItem),
                    AnimeListMainStore.Label.EnableNotificationClick(searchItem)
                )
            ),
            "Expected an enable label per section item among $emittedLabels"
        )
        collectJob.cancel()
    }

    @Test
    fun episodesInfoClickRoutesToLabelOfCurrentlySelectedSection() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val emittedLabels = mutableListOf<AnimeListMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeListMainStore.Intent.EpisodesInfoClick(id = 7))
        store.accept(AnimeListMainStore.Intent.AnnouncedSectionClick)
        store.accept(AnimeListMainStore.Intent.EpisodesInfoClick(id = 8))
        store.accept(AnimeListMainStore.Intent.SearchSectionClick)
        store.accept(AnimeListMainStore.Intent.EpisodesInfoClick(id = 9))

        //Then
        assertTrue(
            emittedLabels.containsAll(
                listOf(
                    AnimeListMainStore.Label.OngoingEpisodeInfoClick(7),
                    AnimeListMainStore.Label.AnnouncedEpisodeInfoClick(8),
                    AnimeListMainStore.Label.SearchEpisodeInfoClick(9)
                )
            ),
            "Expected one episode info label per section among $emittedLabels"
        )
        collectJob.cancel()
    }

    @Test
    fun loadNextPagePublishesLabelForCurrentlySelectedSection() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val emittedLabels = mutableListOf<AnimeListMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeListMainStore.Intent.LoadNextPage)
        store.accept(AnimeListMainStore.Intent.AnnouncedSectionClick)
        store.accept(AnimeListMainStore.Intent.LoadNextPage)
        store.accept(AnimeListMainStore.Intent.SearchSectionClick)
        store.accept(AnimeListMainStore.Intent.LoadNextPage)

        //Then
        assertTrue(
            emittedLabels.containsAll(
                listOf(
                    AnimeListMainStore.Label.LoadNextPageOngoingSection,
                    AnimeListMainStore.Label.LoadNextPageAnnouncedSection,
                    AnimeListMainStore.Label.LoadNextPageSearchSection
                )
            ),
            "Expected one next page label per section among $emittedLabels"
        )
        collectJob.cancel()
    }

    @Test
    fun aSectionClickPublishesItsOpenLabelOnlyWhenTheSelectionChanges() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val emittedLabels = mutableListOf<AnimeListMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeListMainStore.Intent.OngoingsSectionClick)
        store.accept(AnimeListMainStore.Intent.AnnouncedSectionClick)
        store.accept(AnimeListMainStore.Intent.AnnouncedSectionClick)
        store.accept(AnimeListMainStore.Intent.SearchSectionClick)
        store.accept(AnimeListMainStore.Intent.OngoingsSectionClick)

        //Then
        assertEquals(
            listOf(
                AnimeListMainStore.Label.OpenAnnouncedSection,
                AnimeListMainStore.Label.OpenSearchSection,
                AnimeListMainStore.Label.OpenOngoingSection
            ),
            emittedLabels
        )
        assertEquals(SectionHatDomain.ONGOINGS, store.state.selectedSection)
        collectJob.cancel()
    }

    @Test
    fun theSearchBarOpensOnASearchSectionClickAndClosesOnCancel() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val emittedLabels = mutableListOf<AnimeListMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeListMainStore.Intent.SearchSectionClick)

        //Then
        assertEquals(SearchDomain.Type.SHOWN, store.state.search.type)

        //When
        store.accept(AnimeListMainStore.Intent.CancelSearchClick)

        //Then
        assertEquals(SearchDomain.Type.HIDDEN, store.state.search.type)

        //When
        store.accept(AnimeListMainStore.Intent.SearchSectionClick)

        //Then
        assertEquals(SearchDomain.Type.SHOWN, store.state.search.type)
        assertEquals(
            listOf<AnimeListMainStore.Label>(AnimeListMainStore.Label.OpenSearchSection),
            emittedLabels
        )
        collectJob.cancel()
    }

    @Test
    fun changeSearchTextReachesTheStateAndIsForwardedToTheSearchSection() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val emittedLabels = mutableListOf<AnimeListMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeListMainStore.Intent.ChangeSearchText(searchText = "Naruto"))

        //Then
        assertEquals("Naruto", store.state.search.searchText)
        assertEquals(
            listOf<AnimeListMainStore.Label>(
                AnimeListMainStore.Label.ChangeSearchText(searchText = "Naruto")
            ),
            emittedLabels
        )
        collectJob.cancel()
    }

    @Test
    fun updateSectionPublishesTheRefreshLabelOfTheSelectedSection() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val emittedLabels = mutableListOf<AnimeListMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        //When
        store.accept(AnimeListMainStore.Intent.UpdateSection)
        store.accept(AnimeListMainStore.Intent.AnnouncedSectionClick)
        store.accept(AnimeListMainStore.Intent.UpdateSection)
        store.accept(AnimeListMainStore.Intent.SearchSectionClick)
        store.accept(AnimeListMainStore.Intent.UpdateSection)

        //Then
        assertTrue(
            emittedLabels.containsAll(
                listOf(
                    AnimeListMainStore.Label.UpdateOngoingSection,
                    AnimeListMainStore.Label.UpdateAnnouncedSection,
                    AnimeListMainStore.Label.UpdateSearchSection
                )
            ),
            "Expected one update label per section among $emittedLabels"
        )
        collectJob.cancel()
    }

    @Test
    fun changeResetListPositionFlagReachesTheState() = runTest(testDispatcher) {
        //Given
        val store = createStore()

        //When
        store.accept(
            AnimeListMainStore.Intent.ChangeResetListPositionFlag(
                isNeedToResetListPosition = true
            )
        )

        //Then
        assertTrue(store.state.isNeedToResetListPositon)

        //When
        store.accept(
            AnimeListMainStore.Intent.ChangeResetListPositionFlag(
                isNeedToResetListPosition = false
            )
        )

        //Then
        assertFalse(store.state.isNeedToResetListPositon)
    }

    // A section store emits on every dispatch of its own, so this intent arrives constantly.
    // Anything it does through the dispatcher costs the list a frame each time.
    @Test
    fun aContentUpdateThatLeavesTheContentTypeAloneReachesTheStateWithoutADispatch() =
        runTest(testDispatcher) {
            //Given
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val store = createStore()
            val item = testListItem(id = 1)

            //When
            store.accept(
                AnimeListMainStore.Intent.UpdateOngoingContent(
                    content = SectionContentDomain(listItems = listOf(item))
                )
            )

            //Then
            assertEquals(listOf(item), store.state.ongoingContent.listItems)
        }

    @Test
    fun aContentTypeChangeShowsLoadingForTheAnimationBeforeSwitching() = runTest(testDispatcher) {
        //Given
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = createStore()
        store.accept(
            AnimeListMainStore.Intent.UpdateOngoingContent(
                content = SectionContentDomain(contentType = ContentTypeDomain.LOADED)
            )
        )
        advanceUntilIdle()

        //When
        store.accept(
            AnimeListMainStore.Intent.UpdateOngoingContent(
                content = SectionContentDomain(contentType = ContentTypeDomain.ERROR)
            )
        )
        runCurrent()

        //Then
        assertEquals(ContentTypeDomain.LOADING, store.state.ongoingContent.contentType)

        //When
        advanceUntilIdle()

        //Then
        assertEquals(ContentTypeDomain.ERROR, store.state.ongoingContent.contentType)
    }

    @Test
    fun theAnnouncedAndSearchSectionsSwitchContentTypeThroughTheSameLoadingStep() =
        runTest(testDispatcher) {
            //Given
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val store = createStore()
            store.accept(
                AnimeListMainStore.Intent.UpdateAnnouncedContent(
                    content = SectionContentDomain(contentType = ContentTypeDomain.LOADED)
                )
            )
            store.accept(
                AnimeListMainStore.Intent.UpdateSearchContent(
                    content = SectionContentDomain(contentType = ContentTypeDomain.LOADED)
                )
            )
            advanceUntilIdle()

            //When
            store.accept(
                AnimeListMainStore.Intent.UpdateAnnouncedContent(
                    content = SectionContentDomain(contentType = ContentTypeDomain.ERROR)
                )
            )
            store.accept(
                AnimeListMainStore.Intent.UpdateSearchContent(
                    content = SectionContentDomain(contentType = ContentTypeDomain.ERROR)
                )
            )
            runCurrent()

            //Then
            assertEquals(ContentTypeDomain.LOADING, store.state.announcedContent.contentType)
            assertEquals(ContentTypeDomain.LOADING, store.state.searchContent.contentType)

            //When
            advanceUntilIdle()

            //Then
            assertEquals(ContentTypeDomain.ERROR, store.state.announcedContent.contentType)
            assertEquals(ContentTypeDomain.ERROR, store.state.searchContent.contentType)
        }

    @Test
    fun itemsArrivingMidSwitchStillEndOnTheContentTypeThatWasAskedFor() = runTest(testDispatcher) {
        //Given
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = createStore()
        val item = testListItem(id = 1)
        store.accept(
            AnimeListMainStore.Intent.UpdateOngoingContent(
                content = SectionContentDomain(contentType = ContentTypeDomain.LOADED)
            )
        )
        advanceUntilIdle()
        store.accept(
            AnimeListMainStore.Intent.UpdateOngoingContent(
                content = SectionContentDomain(contentType = ContentTypeDomain.ERROR)
            )
        )
        runCurrent()

        //When
        store.accept(
            AnimeListMainStore.Intent.UpdateOngoingContent(
                content = SectionContentDomain(
                    contentType = ContentTypeDomain.ERROR,
                    listItems = listOf(item)
                )
            )
        )
        advanceUntilIdle()

        //Then
        assertEquals(ContentTypeDomain.ERROR, store.state.ongoingContent.contentType)
        assertEquals(listOf(item), store.state.ongoingContent.listItems)
    }

    // A section that restarts while the switch to LOADED is still animating reports the same
    // LOADING that switch already wrote. The main store must follow the section back to LOADING
    // instead of finishing a switch the section has already left behind.
    @Test
    fun aSectionRestartingMidSwitchLeavesTheMainStoreLoading() = runTest(testDispatcher) {
        //Given
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = createStore()
        val item = testListItem(id = 1)
        store.accept(
            AnimeListMainStore.Intent.UpdateOngoingContent(
                content = SectionContentDomain(
                    contentType = ContentTypeDomain.LOADED,
                    listItems = listOf(item)
                )
            )
        )
        runCurrent()
        advanceTimeBy(ANIMATION_DURATION_VERY_SHORT / 2)

        //When
        store.accept(
            AnimeListMainStore.Intent.UpdateOngoingContent(
                content = SectionContentDomain(
                    contentType = ContentTypeDomain.LOADING,
                    listItems = listOf(item)
                )
            )
        )
        advanceUntilIdle()

        //Then
        assertEquals(ContentTypeDomain.LOADING, store.state.ongoingContent.contentType)
    }

    @Test
    fun eachSectionKeepsItsOwnContentUpdatesApart() = runTest(testDispatcher) {
        //Given
        val store = createStore()
        val ongoingItem = testListItem(id = 1)
        val announcedItem = testListItem(id = 2)
        val searchItem = testListItem(id = 3)

        //When
        store.accept(
            AnimeListMainStore.Intent.UpdateOngoingContent(
                content = SectionContentDomain(
                    listItems = listOf(ongoingItem),
                    enabledExtraEpisodesInfoIds = setOf(ongoingItem.id),
                    animeDetails = AnimeDetails(nextEpisodesInfo = mapOf(ongoingItem.id to "today"))
                )
            )
        )
        store.accept(
            AnimeListMainStore.Intent.UpdateAnnouncedContent(
                content = SectionContentDomain(
                    listItems = listOf(announcedItem),
                    enabledExtraEpisodesInfoIds = setOf(announcedItem.id)
                )
            )
        )
        store.accept(
            AnimeListMainStore.Intent.UpdateSearchContent(
                content = SectionContentDomain(
                    listItems = listOf(searchItem),
                    enabledExtraEpisodesInfoIds = setOf(searchItem.id),
                    animeDetails = AnimeDetails(nextEpisodesInfo = mapOf(searchItem.id to "soon"))
                )
            )
        )

        //Then
        assertEquals(listOf(ongoingItem), store.state.ongoingContent.listItems)
        assertEquals(
            setOf(ongoingItem.id),
            store.state.ongoingContent.enabledExtraEpisodesInfoIds
        )
        assertEquals(
            mapOf(ongoingItem.id to "today"),
            store.state.ongoingContent.animeDetails.nextEpisodesInfo
        )
        assertEquals(listOf(announcedItem), store.state.announcedContent.listItems)
        assertEquals(
            setOf(announcedItem.id),
            store.state.announcedContent.enabledExtraEpisodesInfoIds
        )
        assertEquals(listOf(searchItem), store.state.searchContent.listItems)
        assertEquals(setOf(searchItem.id), store.state.searchContent.enabledExtraEpisodesInfoIds)
        assertEquals(
            mapOf(searchItem.id to "soon"),
            store.state.searchContent.animeDetails.nextEpisodesInfo
        )
    }

    @Test
    fun theDelayedContentTypeSwitchRunsInTheStoresOwnScopeAndDiesWithIt() = runTest(testDispatcher) {
        //Given
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val contextProvider = JobRecordingContextProviderFake()
        val store = createStore(contextProvider)
        store.accept(
            AnimeListMainStore.Intent.UpdateOngoingContent(
                content = SectionContentDomain(contentType = ContentTypeDomain.LOADED)
            )
        )
        runCurrent()
        advanceTimeBy(ANIMATION_DURATION_VERY_SHORT / 2)
        val executorJob = assertNotNull(contextProvider.executorJob)
        val coroutinesDuringTheDelay = executorJob.children.count()

        //When
        store.dispose()
        advanceUntilIdle()

        //Then
        assertEquals(1, coroutinesDuringTheDelay, "the switch did not wait in the store's scope")
        assertFalse(executorJob.isActive, "the switch outlived its store")
        assertEquals(ContentTypeDomain.LOADING, store.state.ongoingContent.contentType)
    }

    // The scope a CoroutineExecutor exposes is built straight from this context, so the job
    // handed out here is the one a store's dispose cancels.
    private class JobRecordingContextProviderFake : CoroutineContextProviderBase() {

        override val exceptionHandlerCallback: (Throwable) -> Unit = {}

        var executorJob: Job? = null
            private set

        override fun newMainCoroutineContext(): CoroutineContext =
            super.newMainCoroutineContext().also { executorJob = it[Job] }
    }
}
