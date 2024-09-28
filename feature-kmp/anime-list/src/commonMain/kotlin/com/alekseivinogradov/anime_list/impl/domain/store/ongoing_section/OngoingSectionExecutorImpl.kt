package com.alekseivinogradov.anime_list.impl.domain.store.ongoing_section

import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import com.alekseivinogradov.anime_base.api.domain.AnimeId
import com.alekseivinogradov.anime_base.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.anime_list.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.impl.domain.paging.OngoingListDataSource
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.OngoingUsecases
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

internal class OngoingSectionExecutorImpl(
    private val usecases: OngoingUsecases
) : OngoingSectionExecutor() {

    private var updateSectionJob: Job? = null
    private val updateExtraEpisodesInfoJobMap: MutableMap<AnimeId, Job> = mutableMapOf()

    override fun executeAction(action: OngoingSectionStore.Action) {
        when (action) {
            OngoingSectionStore.Action.InitSection -> initSection()
        }
    }

    override fun executeIntent(intent: OngoingSectionStore.Intent) {
        when (intent) {
            OngoingSectionStore.Intent.OpenSection -> initSection()
            OngoingSectionStore.Intent.UpdateSection -> updateSection()
            is OngoingSectionStore.Intent.EpisodesInfoClick -> episodeInfoClick(intent)
        }
    }

    private fun initSection() {
        if (state().sectionContent.contentType != ContentTypeDomain.LOADED) {
            updateSection()
        }
    }

    private fun updateSection() {
        updateSectionJob?.cancel()
        updateSectionJob = scope.launch {
            dispatch(
                OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADING)
            )
            dispatch(
                OngoingSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                    enabledExtraEpisodesInfoId = setOf()
                )
            )
            dispatch(
                OngoingSectionStore.Message.UpdateNextEpisodesInfo(
                    nextEpisodesInfo = mapOf()
                )
            )
            getPagingDataFlow().collect { listItems: PagingData<ListItemDomain> ->
                dispatch(OngoingSectionStore.Message.UpdateListItems(listItems))
                dispatch(
                    OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADED)
                )
            }
        }
    }

    private fun getPagingDataFlow(): Flow<PagingData<ListItemDomain>> {
        return Pager(
            config = PagingConfig(pageSize = ITEMS_PER_PAGE)
        ) {
            OngoingListDataSource(
                fetchOngoingAnimeListUseCase = usecases.fetchOngoingAnimeListUsecase
            )
        }.flow.cachedIn(scope)
    }

    private fun episodeInfoClick(intent: OngoingSectionStore.Intent.EpisodesInfoClick) {
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
            OngoingSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
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
            OngoingSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                newEnabledExtraEpisodesInfoIds
            )
        )
    }

    private fun updateExtraEpisodesInfo(id: AnimeId) {
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
            OngoingSectionStore.Message.UpdateNextEpisodesInfo(newNextEpisodesInfo)
        )
    }
}
