package com.alekseivinogradov.anime_list.impl.domain.store.search_section

import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import com.alekseivinogradov.anime_base.api.domain.AnimeId
import com.alekseivinogradov.anime_base.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.anime_base.api.domain.SEARCH_DEBOUNCE
import com.alekseivinogradov.anime_list.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore
import com.alekseivinogradov.anime_list.impl.domain.paging.SearchListDataSource
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.SearchUsecases
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
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
            dispatch(
                SearchSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                    enabledExtraEpisodesInfoId = setOf()
                )
            )
            dispatch(
                SearchSectionStore.Message.UpdateNextEpisodesInfo(
                    nextEpisodesInfo = mapOf()
                )
            )
            getPagingDataFlow().collect { listItems: PagingData<ListItemDomain> ->
                dispatch(SearchSectionStore.Message.UpdateListItems(listItems))
                dispatch(
                    SearchSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADED)
                )
            }
        }
    }

    private fun getPagingDataFlow(): Flow<PagingData<ListItemDomain>> {
        return Pager(
            config = PagingConfig(pageSize = ITEMS_PER_PAGE)
        ) {
            SearchListDataSource(
                fetchAnimeListBySearchUsecase = usecases.fetchAnimeListBySearchUsecase,
                searchText = state().searchText
            )
        }.flow.cachedIn(scope)
    }

    private fun ChangesearchText(intent: SearchSectionStore.Intent.ChangeSearchText) {
        dispatch(SearchSectionStore.Message.ChangeSearchText(intent.searchText))
        searchFlow?.update { state().searchText }
    }

    private fun episodeInfoClick(intent: SearchSectionStore.Intent.EpisodesInfoClick) {
        if (state().sectionContent.enabledExtraEpisodesInfoIds.contains(intent.listItem.id)) {
            availableEpisodesInfoClick(intent.listItem)
        } else {
            extraEpisodesInfoClick(intent.listItem)
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
        if (
            listItem.releaseStatus == ReleaseStatusDomain.ONGOING &&
            !state().sectionContent.nextEpisodesInfo.contains(listItem.id)
        ) {
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
        val newNextEpisodesInfo = mutableMapOf<AnimeId, String>()
            .apply {
                putAll(state().sectionContent.nextEpisodesInfo)
                updateListItem.nextEpisodeAt?.let {
                    this[updateListItem.id] = it
                }
            }.toMap()
        dispatch(
            SearchSectionStore.Message.UpdateNextEpisodesInfo(newNextEpisodesInfo)
        )
    }
}
