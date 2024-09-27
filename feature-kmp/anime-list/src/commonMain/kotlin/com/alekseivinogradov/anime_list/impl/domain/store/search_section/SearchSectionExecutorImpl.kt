package com.alekseivinogradov.anime_list.impl.domain.store.search_section

import com.alekseivinogradov.anime_base.api.domain.AnimeId
import com.alekseivinogradov.anime_base.api.domain.SEARCH_DEBOUNCE
import com.alekseivinogradov.anime_list.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.ReleaseStatusDomain
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

    private var searchFlow: MutableStateFlow<String>? = null
    private var changeSearchJob: Job? = null
    private var updateSectionJob: Job? = null
    private val updateExtraEpisodesInfoJobMap: MutableMap<AnimeId, Job> = mutableMapOf()

    override fun executeIntent(intent: SearchSectionStore.Intent) {
        when (intent) {
            SearchSectionStore.Intent.OpenSection -> initSection()
            SearchSectionStore.Intent.UpdateSection -> updateSection()

            is SearchSectionStore.Intent.ChangeSearchText -> ChangesearchText(intent)

            is SearchSectionStore.Intent.EpisodesInfoClick -> episodeInfoClick(intent)
        }
    }

    private fun initSection() {
        subscribeSearchFlowIfNeeded()
    }

    @OptIn(FlowPreview::class)
    private fun subscribeSearchFlowIfNeeded() {
        if (changeSearchJob?.isActive == true) return
        searchFlow = MutableStateFlow(state().searchText)
        changeSearchJob = scope.launch {
            searchFlow?.debounce(SEARCH_DEBOUNCE)
                ?.collect {
                    updateSection()
                }
        }
    }

    private fun updateSection() {
        updateSectionJob?.cancel()
        updateSectionJob = scope.launch {
            dispatch(
                SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADING)
            )
            val result = usecases
                .fetchAnimeListBySearchUsecase
                .execute(
                    page = 1,
                    searchText = state().searchText
                )
            when (result) {
                is CallResult.Success -> {
                    dispatch(
                        SearchSectionStore.Message.UpdateListItems(result.value)
                    )
                    if (result.value.isNotEmpty()) {
                        dispatch(
                            SearchSectionStore.Message.ChangeContentType(
                                ContentTypeDomain.LOADED
                            )
                        )
                    } else {
                        dispatch(
                            SearchSectionStore.Message.ChangeContentType(
                                ContentTypeDomain.ERROR
                            )
                        )
                    }
                }

                is CallResult.HttpError,
                is CallResult.OtherError -> dispatch(
                    SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)
                )
            }
            dispatch(
                SearchSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                    enabledExtraEpisodesInfoId = setOf()
                )
            )
        }
    }

    private fun ChangesearchText(intent: SearchSectionStore.Intent.ChangeSearchText) {
        dispatch(SearchSectionStore.Message.ChangeSearchText(intent.searchText))
        searchFlow?.update { state().searchText }
    }

    private fun episodeInfoClick(intent: SearchSectionStore.Intent.EpisodesInfoClick) {
        val sectionContent = state().sectionContent
        val listItem = sectionContent.listItems
            .find { it.id == intent.id } ?: return

        if (sectionContent.enabledExtraEpisodesInfoIds.contains(listItem.id)) {
            availableEpisodesInfoClick(listItem)
        } else {
            extraEpisodesInfoClick(listItem)
        }
    }

    private fun extraEpisodesInfoClick(listItem: ListItemDomain) {
        val newEnabledExtraEpisodesInfoIds = mutableSetOf<AnimeId>().apply {
            addAll(state().sectionContent.enabledExtraEpisodesInfoIds)
            add(listItem.id)
        }.toSet()
        dispatch(
            SearchSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                newEnabledExtraEpisodesInfoIds
            )
        )
        if (listItem.releaseStatus == ReleaseStatusDomain.ONGOING) {
            updateExtraEpisodesInfo(listItem.id)
        }
    }

    private fun availableEpisodesInfoClick(listItem: ListItemDomain) {
        val newEnabledExtraEpisodesInfoIds = mutableSetOf<AnimeId>().apply {
            addAll(state().sectionContent.enabledExtraEpisodesInfoIds)
            remove(listItem.id)
        }.toSet()
        dispatch(
            SearchSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                newEnabledExtraEpisodesInfoIds
            )
        )
    }

    private fun updateExtraEpisodesInfo(id: Int) {
        updateExtraEpisodesInfoJobMap[id]?.cancel()
        updateExtraEpisodesInfoJobMap[id] = scope.launch {
            val result = usecases
                .fetchAnimeByIdUsecase
                .execute(id)

            when (result) {
                is CallResult.Success -> onSuccessUpdateExtraEpisodesInfo(
                    updateListItem = result.value
                )

                is CallResult.HttpError,
                is CallResult.OtherError -> Unit
            }
        }
    }

    private fun onSuccessUpdateExtraEpisodesInfo(
        updateListItem: ListItemDomain
    ) {
        val newListItems = state().sectionContent.listItems.map {
            if (it.id == updateListItem.id) {
                it.copy(nextEpisodeAt = updateListItem.nextEpisodeAt)
            } else it
        }
        dispatch(
            SearchSectionStore.Message.UpdateListItems(
                listItems = newListItems.toList()
            )
        )
    }
}
