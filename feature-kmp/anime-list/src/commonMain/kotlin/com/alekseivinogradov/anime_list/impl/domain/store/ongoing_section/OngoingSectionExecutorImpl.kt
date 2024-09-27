package com.alekseivinogradov.anime_list.impl.domain.store.ongoing_section

import com.alekseivinogradov.anime_base.api.domain.AnimeId
import com.alekseivinogradov.anime_list.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.OngoingUsecases
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.Job
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
        if (state().sectionContent.listItems.isEmpty()) {
            updateSection()
        }
    }

    private fun updateSection() {
        updateSectionJob?.cancel()
        updateSectionJob = scope.launch {
            dispatch(
                OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADING)
            )
            val result = usecases
                .fetchOngoingAnimeListUsecase
                .execute(page = 1)
            when (result) {
                is CallResult.Success -> {
                    dispatch(
                        OngoingSectionStore.Message.UpdateListItems(result.value)
                    )
                    if (result.value.isNotEmpty()) {
                        dispatch(
                            OngoingSectionStore.Message.ChangeContentType(
                                ContentTypeDomain.LOADED
                            )
                        )
                    } else {
                        dispatch(
                            OngoingSectionStore.Message.ChangeContentType(
                                ContentTypeDomain.ERROR
                            )
                        )
                    }
                }

                is CallResult.HttpError,
                is CallResult.OtherError -> dispatch(
                    OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)
                )
            }
            dispatch(
                OngoingSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                    enabledExtraEpisodesInfoId = setOf()
                )
            )
        }
    }

    private fun episodeInfoClick(intent: OngoingSectionStore.Intent.EpisodesInfoClick) {
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
            OngoingSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
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
        val newListItems = state().sectionContent.listItems.map {
            if (it.id == updateListItem.id) {
                it.copy(nextEpisodeAt = updateListItem.nextEpisodeAt)
            } else it
        }
        dispatch(
            OngoingSectionStore.Message.UpdateListItems(
                listItems = newListItems
            )
        )
    }
}
