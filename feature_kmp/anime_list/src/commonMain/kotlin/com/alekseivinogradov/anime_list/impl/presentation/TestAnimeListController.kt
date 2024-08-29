package com.alekseivinogradov.anime_list.impl.presentation

import com.alekseivinogradov.anime_list.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anime_list.api.presentation.model.SearchUi
import com.alekseivinogradov.anime_list.api.presentation.model.SectionUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.EpisodesInfoTypeUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.ListItemUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.NotificationUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.ReleaseStatusUi
import com.alekseivinogradov.anime_list.api.presentation.AnimeListView
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TestAnimeListController(lifecycle: Lifecycle) {

    private val testCoroutineScope = CoroutineScope(Dispatchers.Main)
    private val testUiDataFlow =
        MutableStateFlow<AnimeListView.UiModel>(AnimeListView.UiModel())

    init {
        testCoroutineScope.launch {
            while (true) {
                testUiDataFlow.update { createTestData() }
//                iteration++
                delay(10000)
            }
        }
        lifecycle.doOnDestroy { testCoroutineScope.cancel() }
    }

    fun onViewCreated(mainView: AnimeListView, viewLifecycle: Lifecycle) {
        bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            subscribeOnAllRequiredStates() bindTo mainView
        }
    }

    private fun subscribeOnAllRequiredStates(): Flow<AnimeListView.UiModel> {
        return testUiDataFlow
    }

    private fun createTestData(): AnimeListView.UiModel {
        return AnimeListView.UiModel(
            selectedSection = testNextSelectedSection(),
            search = testNextSearch(),
            contentType = ContentTypeUi.LOADED,
            ongoingListItems = testCreateOngoingListItems(),
            announcedListItems = testCreateAnnouncedListItems(),
            searchListItems = testCreateSearchListItems()
        )
    }

    private fun testNextSelectedSection(): SectionUi {
        return when (testUiDataFlow.value.selectedSection) {
            SectionUi.ONGOINGS -> SectionUi.ANNOUNCED
            SectionUi.ANNOUNCED -> SectionUi.SEARCH
            SectionUi.SEARCH -> SectionUi.ONGOINGS
        }
    }

    private fun testNextSearch(): SearchUi {
        return when (testUiDataFlow.value.search) {
            SearchUi.HIDEN -> SearchUi.SHOWN
            SearchUi.SHOWN -> SearchUi.HIDEN
        }
    }

    private fun testNextContentType(): ContentTypeUi {
        return when (testUiDataFlow.value.contentType) {
            ContentTypeUi.LOADED -> ContentTypeUi.LOADING
            ContentTypeUi.LOADING -> ContentTypeUi.NO_DATA
            ContentTypeUi.NO_DATA -> ContentTypeUi.LOADED
        }
    }

    private fun testCreateOngoingListItems(): List<ListItemUi> {
        val listCapacity = 20
        return mutableListOf<ListItemUi>().apply {
            while (size < listCapacity) {
                add(
                    index = size,
                    element = testCreateListItem(
                        index = size,
                        textPrefix = "O",
                        iteration = iteration
                    )
                )
            }
        }
    }

    private fun testCreateAnnouncedListItems(): List<ListItemUi> {
        val listCapacity = 20
        return mutableListOf<ListItemUi>().apply {
            while (size < listCapacity) {
                add(
                    index = size,
                    element = testCreateListItem(
                        index = size,
                        textPrefix = "A",
                        iteration = iteration
                    )
                )
            }
        }
    }

    private fun testCreateSearchListItems(): List<ListItemUi> {
        val listCapacity = 20
        return mutableListOf<ListItemUi>().apply {
            while (size < listCapacity) {
                add(
                    index = size,
                    element = testCreateListItem(
                        index = size,
                        textPrefix = "S",
                        iteration = iteration
                    )
                )
            }
        }
    }

    private var iteration = 0
    private fun testCreateListItem(index: Int, textPrefix: String, iteration: Int): ListItemUi {
        val firstItem = testUiDataFlow.value.ongoingListItems.getOrNull(0)
        return ListItemUi(
            itemIndex = index,
            imageUrl = "https://cdn.oneesports.gg/cdn-data/2022/01/AttackonTitan_FinalSeasonKeyVisualEren-1.jpg",
            name = "$textPrefix n$index i$iteration",
            episodesInfoType = EpisodesInfoTypeUi.AVAILABLE,
//            episodesInfoType = testNextEpisodesInfoType(firstItem),
            availableEpisodesInfo = "$textPrefix n$index i$iteration",
            extraEpisodesInfo = "$textPrefix n$index i$iteration",
            score = "$textPrefix n$index i$iteration",
            releaseStatus = ReleaseStatusUi.ONGOING,
//            releaseStatus = testNextReleaseStatus(firstItem),
            notification = NotificationUi.DISABLED
//            notification = testNextNotification(firstItem)
        )
    }

    private fun testNextImageUrl(): String {
        return if (iteration % 2 == 0) {
            "https://cdn.oneesports.gg/cdn-data/2022/01/AttackonTitan_FinalSeasonKeyVisualEren-1.jpg"
        } else {
            "https://wallpapercat.com/w/full/9/2/6/25951-3840x2160-desktop-4k-attack-on-titan-the-final-season-wallpaper-photo.jpg"
        }
    }

    private fun testNextEpisodesInfoType(listItems: ListItemUi?): EpisodesInfoTypeUi {
        return when (listItems?.episodesInfoType) {
            null, EpisodesInfoTypeUi.AVAILABLE -> EpisodesInfoTypeUi.FUTURE
            EpisodesInfoTypeUi.FUTURE -> EpisodesInfoTypeUi.AVAILABLE
        }
    }

    private fun testNextReleaseStatus(listItems: ListItemUi?): ReleaseStatusUi {
        return when (listItems?.releaseStatus) {
            null, ReleaseStatusUi.ONGOING -> ReleaseStatusUi.ANNOUNCED
            ReleaseStatusUi.ANNOUNCED -> ReleaseStatusUi.RELEASED
            ReleaseStatusUi.RELEASED -> ReleaseStatusUi.UNKNOWN
            ReleaseStatusUi.UNKNOWN -> ReleaseStatusUi.ONGOING
        }
    }

    private fun testNextNotification(listItems: ListItemUi?): NotificationUi {
        return when (listItems?.notification) {
            null, NotificationUi.ENABLED -> NotificationUi.DISABLED
            NotificationUi.DISABLED -> NotificationUi.ENABLED
        }
    }
}