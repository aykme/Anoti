package com.alekseivinogradov.anime_list.impl.domain.store.search_section

import com.alekseivinogradov.anime_list.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.anime_list.api.domain.model.section.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.section.EpisodesInfoTypeDomain
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeListBySearchUsecase
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private var updateSectionJob: Job? = null

internal class SearchSectionExecutorImpl(
    private val fetchAnimeListUsecase: FetchAnimeListBySearchUsecase
) : SearchSectionExecutor() {

    override fun executeIntent(intent: SearchSectionStore.Intent) {
        when (intent) {
            SearchSectionStore.Intent.InitSection -> initSection()
            SearchSectionStore.Intent.UpdateSection -> updateSection()
            is SearchSectionStore.Intent.EpisodesInfoClick -> episodeInfoClick(intent.itemIndex)
            is SearchSectionStore.Intent.NotificationClick -> notificationClick()
        }
    }

    private fun initSection() {
        if (state().listItems.isEmpty()) {
            updateSection()
        }
    }

    private fun updateSection() {
        updateSectionJob?.cancel()
        updateSectionJob = scope.launch {
            dispatch(
                SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADING)
            )
            val result = fetchAnimeListUsecase.execute(
                page = 1,
                itemsPerPage = ITEMS_PER_PAGE,
                searchText = ""
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