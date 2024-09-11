package com.alekseivinogradov.anime_list.impl.domain.store.search_section

import com.alekseivinogradov.anime_list.api.domain.AnimeId
import com.alekseivinogradov.anime_list.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.anime_list.api.domain.SEARCH_DEBOUNCE
import com.alekseivinogradov.anime_list.api.domain.model.section.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.section.EpisodesInfoTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.section.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.section.ReleaseStatusDomain
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.SearchUsecases
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SearchSectionExecutorImpl(
    private val usecases: SearchUsecases
) : SearchSectionExecutor() {

    private val searchFlow: MutableStateFlow<String> = MutableStateFlow("")
    private var changeSearchJob: Job? = null
    private var updateSectionJob: Job? = null
    private val updateExtraEpisodesInfoJobMap = mutableMapOf<AnimeId, Job>()

    override fun executeIntent(intent: SearchSectionStore.Intent) {
        when (intent) {
            is SearchSectionStore.Intent.UpdateEnabledNotificationIds -> {
                updateEnabledNotificationIds(intent.enabledNotificationIds)
            }

            SearchSectionStore.Intent.InitSection -> initSection()
            SearchSectionStore.Intent.UpdateSection -> updateSection(state().searchText)
            is SearchSectionStore.Intent.SearchTextChange -> searchTextChange(intent.searchText)
            is SearchSectionStore.Intent.EpisodesInfoClick -> episodeInfoClick(intent.itemIndex)
            is SearchSectionStore.Intent.NotificationClick -> notificationClick(intent.itemIndex)
        }
    }

    private fun updateEnabledNotificationIds(enabledNotificationIds: Set<AnimeId>) {
        dispatch(SearchSectionStore.Message.UpdateEnabledNotificationIds(enabledNotificationIds))
    }

    private fun initSection() {
        subscribeSearchFlow()
    }

    @OptIn(FlowPreview::class)
    private fun subscribeSearchFlow() {
        if (changeSearchJob?.isActive == true) return
        changeSearchJob = scope.launch {
            searchFlow.debounce(SEARCH_DEBOUNCE)
                .collect {
                    updateSection(state().searchText)
                }
        }
    }

    private fun updateSection(searchText: String) {
        updateSectionJob?.cancel()
        updateSectionJob = scope.launch {
            dispatch(
                SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADING)
            )
            val result = usecases.fetchAnimeListBySearchUsecase.execute(
                page = 1,
                itemsPerPage = ITEMS_PER_PAGE,
                searchText = searchText
            )
            when (result) {
                is CallResult.Success -> {
                    dispatch(
                        SearchSectionStore.Message.UpdateListItems(result.value)
                    )
                    if (result.value.isNotEmpty()) {
                        dispatch(
                            SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADED)
                        )
                    } else {
                        dispatch(
                            SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.NO_DATA)
                        )
                    }
                }

                is CallResult.HttpError,
                is CallResult.OtherError -> dispatch(
                    SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.NO_DATA)
                )
            }
        }
    }

    private fun searchTextChange(searchText: String) {
        dispatch(SearchSectionStore.Message.ChangeSearchText(searchText))
        searchFlow.update { state().searchText }
    }

    private fun episodeInfoClick(itemIndex: Int) {
        val listItem = state().listItems.getOrNull(itemIndex) ?: return
        when (listItem.episodesInfoType) {
            EpisodesInfoTypeDomain.AVAILABLE -> {
                extraEpisodesInfoClick(listItem = listItem, itemIndex = itemIndex)
            }

            EpisodesInfoTypeDomain.EXTRA -> {
                availableEpisodesInfoClick(listItem = listItem, itemIndex = itemIndex)
            }
        }
    }

    private fun extraEpisodesInfoClick(listItem: ListItemDomain, itemIndex: Int) {
        val newListItem = listItem.copy(
            episodesInfoType = EpisodesInfoTypeDomain.EXTRA
        )
        val newListItems = state().listItems.toMutableList()
        newListItems[itemIndex] = newListItem
        dispatch(
            SearchSectionStore.Message.UpdateListItems(
                listItems = newListItems.toList()
            )
        )
        if (listItem.releaseStatus == ReleaseStatusDomain.ONGOING) {
            updateExtraEpisodesInfo(itemIndex)
        }
    }

    private fun availableEpisodesInfoClick(listItem: ListItemDomain, itemIndex: Int) {
        val newListItem = listItem.copy(
            episodesInfoType = EpisodesInfoTypeDomain.AVAILABLE
        )
        val newListItems = state().listItems.toMutableList()
        newListItems[itemIndex] = newListItem
        dispatch(
            SearchSectionStore.Message.UpdateListItems(
                listItems = newListItems.toList()
            )
        )
    }

    private fun updateExtraEpisodesInfo(itemIndex: Int) {
        val listItem = state().listItems.getOrNull(itemIndex) ?: return
        val id = listItem.id ?: return

        updateExtraEpisodesInfoJobMap[id]?.cancel()
        updateExtraEpisodesInfoJobMap[id] = scope.launch {
            val result = usecases.fetchAnimeByIdUsecase.execute(id)
            when (result) {
                is CallResult.Success -> onSuccessUpdateExtraEpisodesInfo(
                    updateListItem = result.value,
                    itemIndex = itemIndex
                )

                is CallResult.HttpError,
                is CallResult.OtherError -> Unit
            }
        }
    }

    private fun onSuccessUpdateExtraEpisodesInfo(
        updateListItem: ListItemDomain,
        itemIndex: Int
    ) {
        val currentListItem = state().listItems.getOrNull(itemIndex) ?: return
        val newListItem = currentListItem.copy(
            extraEpisodesInfo = updateListItem.extraEpisodesInfo
        )
        val newListItems = state().listItems.toMutableList()
        newListItems[itemIndex] = newListItem
        dispatch(
            SearchSectionStore.Message.UpdateListItems(
                listItems = newListItems.toList()
            )
        )
    }

    private fun notificationClick(itemIndex: Int) {
        val listItem = state().listItems.getOrNull(itemIndex) ?: return
        val id = listItem.id ?: return

        if (state().enabledNotificationIds.contains(id).not()) {
            enableNotification(listItem)
        } else {
            disableNotification(id)
        }
    }

    private fun enableNotification(listItem: ListItemDomain) {
        publish(SearchSectionStore.Label.EnableNotification(listItem))
    }

    private fun disableNotification(id: Int) {
        publish(SearchSectionStore.Label.DisableNotification(id))
    }
}