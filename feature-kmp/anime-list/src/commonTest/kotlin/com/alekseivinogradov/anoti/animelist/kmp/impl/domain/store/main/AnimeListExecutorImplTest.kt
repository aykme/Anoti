package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.main

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionContentDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderBase
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeListExecutorImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createStore(): AnimeListMainStore {
        val coroutineContextProvider = object : CoroutineContextProviderBase() {
            override val exceptionHandlerCallback: (Throwable) -> Unit = {}
        }
        val executorFactory: AnimeListExecutorFactory = {
            AnimeListExecutorImpl(coroutineContextProvider = coroutineContextProvider)
        }
        return AnimeListMainStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = executorFactory
        ).create()
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
        val store = createStore()
        val item = testListItem(id = 1)
        store.accept(
            AnimeListMainStore.Intent.UpdateOngoingContent(
                content = SectionContentDomain(listItems = listOf(item))
            )
        )

        val emittedLabels = mutableListOf<AnimeListMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        store.accept(AnimeListMainStore.Intent.NotificationClick(id = item.id))

        assertTrue(
            emittedLabels.contains(AnimeListMainStore.Label.EnableNotificationClick(item)),
            "Expected EnableNotificationClick($item) among $emittedLabels"
        )
        collectJob.cancel()
    }

    @Test
    fun notificationClickOnAlreadyEnabledIdPublishesDisableWithId() = runTest(testDispatcher) {
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

        store.accept(AnimeListMainStore.Intent.NotificationClick(id = item.id))

        assertEquals(
            listOf<AnimeListMainStore.Label>(AnimeListMainStore.Label.DisableNotificationClick(item.id)),
            emittedLabels
        )
        collectJob.cancel()
    }

    @Test
    fun notificationClickOnUnknownIdPublishesNothing() = runTest(testDispatcher) {
        val store = createStore()
        store.accept(
            AnimeListMainStore.Intent.UpdateOngoingContent(
                content = SectionContentDomain(listItems = listOf(testListItem(id = 1)))
            )
        )

        val emittedLabels = mutableListOf<AnimeListMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        store.accept(AnimeListMainStore.Intent.NotificationClick(id = 999))

        assertTrue(emittedLabels.isEmpty(), "Expected no labels, got $emittedLabels")
        collectJob.cancel()
    }

    @Test
    fun episodesInfoClickRoutesToLabelOfCurrentlySelectedSection() = runTest(testDispatcher) {
        val store = createStore()
        store.accept(AnimeListMainStore.Intent.SearchSectionClick)

        val emittedLabels = mutableListOf<AnimeListMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        store.accept(AnimeListMainStore.Intent.EpisodesInfoClick(id = 7))

        assertTrue(
            emittedLabels.contains(AnimeListMainStore.Label.SearchEpisodeInfoClick(7)),
            "Expected SearchEpisodeInfoClick(7) among $emittedLabels"
        )
        collectJob.cancel()
    }

    @Test
    fun loadNextPagePublishesLabelForCurrentlySelectedSection() = runTest(testDispatcher) {
        val store = createStore()
        store.accept(AnimeListMainStore.Intent.AnnouncedSectionClick)

        val emittedLabels = mutableListOf<AnimeListMainStore.Label>()
        val collectJob = launch { store.labels.collect { emittedLabels.add(it) } }

        store.accept(AnimeListMainStore.Intent.LoadNextPage)

        assertTrue(
            emittedLabels.contains(AnimeListMainStore.Label.LoadNextPageAnnouncedSection),
            "Expected LoadNextPageAnnouncedSection among $emittedLabels"
        )
        collectJob.cancel()
    }
}
