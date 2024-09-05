package com.alekseivinogradov.anime_list.impl.domain.store.search_section

import com.alekseivinogradov.anime_list.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.anime_list.api.domain.SEARCH_DEBOUNCE
import com.alekseivinogradov.anime_list.api.domain.model.section.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.section.EpisodesInfoTypeDomain
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeListBySearchUsecase
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SearchSectionExecutorImpl(
    private val fetchAnimeListUsecase: FetchAnimeListBySearchUsecase
) : SearchSectionExecutor() {

    private val searchFlow: MutableStateFlow<String> = MutableStateFlow("")
    private var changeSearchJob: Job? = null
    private var updateSectionJob: Job? = null

    override fun executeIntent(intent: SearchSectionStore.Intent) {
        when (intent) {
            SearchSectionStore.Intent.InitSection -> initSection()
            SearchSectionStore.Intent.UpdateSection -> updateSection(state().searchText)
            is SearchSectionStore.Intent.SearchTextChange -> searchTextChange(intent.searchText)
            is SearchSectionStore.Intent.EpisodesInfoClick -> episodeInfoClick(intent.itemIndex)
            is SearchSectionStore.Intent.NotificationClick -> notificationClick()
        }
    }

    @OptIn(FlowPreview::class)
    private fun initSection() {
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
            val result = fetchAnimeListUsecase.execute(
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
        val listItem = state().listItems.getOrNull(itemIndex)
        when (listItem?.episodesInfoType) {
            EpisodesInfoTypeDomain.AVAILABLE -> {
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
            }

            EpisodesInfoTypeDomain.EXTRA -> {
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

            null -> Unit
        }
    }

    private fun notificationClick() {

    }
}