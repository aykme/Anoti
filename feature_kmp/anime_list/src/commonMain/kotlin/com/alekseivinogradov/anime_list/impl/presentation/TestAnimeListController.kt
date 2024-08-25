package com.alekseivinogradov.anime_list.impl.presentation

import com.alekseivinogradov.anime_list.api.model.UiContentType
import com.alekseivinogradov.anime_list.api.model.UiSearch
import com.alekseivinogradov.anime_list.api.model.UiSection
import com.alekseivinogradov.anime_list.api.model.list_content.UiEpisodesInfoType
import com.alekseivinogradov.anime_list.api.model.list_content.UiListItem
import com.alekseivinogradov.anime_list.api.model.list_content.UiNotification
import com.alekseivinogradov.anime_list.api.model.list_content.UiReleaseStatus
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
            contentType = UiContentType.LOADED,
            ongoingListItems = testCreateOngoingListItems(),
            announcedListItems = testCreateAnnouncedListItems(),
            searchListItems = testCreateSearchListItems()
        )
    }

    private fun testNextSelectedSection(): UiSection {
        return when (testUiDataFlow.value.selectedSection) {
            UiSection.ONGOINGS -> UiSection.ANNOUNCED
            UiSection.ANNOUNCED -> UiSection.SEARCH
            UiSection.SEARCH -> UiSection.ONGOINGS
        }
    }

    private fun testNextSearch(): UiSearch {
        return when (testUiDataFlow.value.search) {
            UiSearch.HIDEN -> UiSearch.SHOWN
            UiSearch.SHOWN -> UiSearch.HIDEN
        }
    }

    private fun testNextContentType(): UiContentType {
        return when (testUiDataFlow.value.contentType) {
            UiContentType.LOADED -> UiContentType.LOADING
            UiContentType.LOADING -> UiContentType.NO_DATA
            UiContentType.NO_DATA -> UiContentType.LOADED
        }
    }

    private fun testCreateOngoingListItems(): List<UiListItem> {
        val listCapacity = 20
        return mutableListOf<UiListItem>().apply {
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

    private fun testCreateAnnouncedListItems(): List<UiListItem> {
        val listCapacity = 20
        return mutableListOf<UiListItem>().apply {
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

    private fun testCreateSearchListItems(): List<UiListItem> {
        val listCapacity = 20
        return mutableListOf<UiListItem>().apply {
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
    private fun testCreateListItem(index: Int, textPrefix: String, iteration: Int): UiListItem {
        val firstItem = testUiDataFlow.value.ongoingListItems.getOrNull(0)
        return UiListItem(
            itemIndex = index,
            imageUrl = "https://cdn.oneesports.gg/cdn-data/2022/01/AttackonTitan_FinalSeasonKeyVisualEren-1.jpg",
            name = "$textPrefix n$index i$iteration",
            episodesInfoType = UiEpisodesInfoType.AVAILABLE,
//            episodesInfoType = testNextEpisodesInfoType(firstItem),
            availableEpisodesInfo = "$textPrefix n$index i$iteration",
            futureInfo = "$textPrefix n$index i$iteration",
            score = "$textPrefix n$index i$iteration",
            releaseStatus = UiReleaseStatus.ONGOING,
//            releaseStatus = testNextReleaseStatus(firstItem),
            notification = UiNotification.DISABLED
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

    private fun testNextEpisodesInfoType(listItems: UiListItem?): UiEpisodesInfoType {
        return when (listItems?.episodesInfoType) {
            null, UiEpisodesInfoType.AVAILABLE -> UiEpisodesInfoType.FUTURE
            UiEpisodesInfoType.FUTURE -> UiEpisodesInfoType.AVAILABLE
        }
    }

    private fun testNextReleaseStatus(listItems: UiListItem?): UiReleaseStatus {
        return when (listItems?.releaseStatus) {
            null, UiReleaseStatus.ONGOING -> UiReleaseStatus.ANNOUNCED
            UiReleaseStatus.ANNOUNCED -> UiReleaseStatus.RELEASED
            UiReleaseStatus.RELEASED -> UiReleaseStatus.ONGOING
        }
    }

    private fun testNextNotification(listItems: UiListItem?): UiNotification {
        return when (listItems?.notification) {
            null, UiNotification.ENABLED -> UiNotification.DISABLED
            UiNotification.DISABLED -> UiNotification.ENABLED
        }
    }
}