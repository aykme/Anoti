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

class AnimeListController(lifecycle: Lifecycle) {

    private val testCoroutineScope = CoroutineScope(Dispatchers.Main)
    private val testUiDataFlow =
        MutableStateFlow<AnimeListView.UiModel>(AnimeListView.UiModel())

    init {
        testCoroutineScope.launch {
            while (true) {
                delay(2000)
                testUiDataFlow.update { createTestData() }
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
            selectedSection = UiSection.ONGOINGS,
            search = UiSearch.HIDEN,
            contentType = UiContentType.LOADED,
            ongoingListItems = testCreateOngoingListItems()
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
                    )
                )
            }
        }
    }

    private fun testCreateListItem(index: Int, textPrefix: String): UiListItem {
        return UiListItem(
            itemIndex = index,
            imageUrl = "https://cdn.oneesports.gg/cdn-data/2022/01/AttackonTitan_FinalSeasonKeyVisualEren-1.jpg",
            name = "$textPrefix n$index",
            episodesInfoType = UiEpisodesInfoType.CURRENT,
            availableEpisodesInfo = "$textPrefix n$index",
            futureInfo = "$textPrefix n$index",
            score = "$textPrefix n$index",
            releaseStatus = UiReleaseStatus.ONGOING,
            notification = UiNotification.DISABLED
        )
    }
}