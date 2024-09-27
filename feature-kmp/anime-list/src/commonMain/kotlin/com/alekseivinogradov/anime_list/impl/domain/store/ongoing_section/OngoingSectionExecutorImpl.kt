package com.alekseivinogradov.anime_list.impl.domain.store.ongoing_section

import com.alekseivinogradov.anime_base.api.domain.AnimeId
import com.alekseivinogradov.anime_list.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.EpisodesInfoTypeDomain
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
                                ContentTypeDomain.NO_DATA
                            )
                        )
                    }
                }

                is CallResult.HttpError,
                is CallResult.OtherError -> dispatch(
                    OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.NO_DATA)
                )
            }
        }
    }

    private fun episodeInfoClick(intent: OngoingSectionStore.Intent.EpisodesInfoClick) {
        val listItem = state().sectionContent.listItems
            .getOrNull(intent.itemIndex) ?: return

        when (listItem.episodesInfoType) {
            EpisodesInfoTypeDomain.AVAILABLE -> {
                extraEpisodesInfoClick(listItem = listItem, itemIndex = intent.itemIndex)
            }

            EpisodesInfoTypeDomain.EXTRA -> {
                availableEpisodesInfoClick(listItem = listItem, itemIndex = intent.itemIndex)
            }
        }
    }

    private fun extraEpisodesInfoClick(listItem: ListItemDomain, itemIndex: Int) {
        val newListItem = listItem.copy(
            episodesInfoType = EpisodesInfoTypeDomain.EXTRA
        )
        val newListItems = state().sectionContent.listItems
            .toMutableList()

        newListItems[itemIndex] = newListItem
        dispatch(
            OngoingSectionStore.Message.UpdateListItems(
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
        val newListItems = state().sectionContent.listItems
            .toMutableList()

        newListItems[itemIndex] = newListItem
        dispatch(
            OngoingSectionStore.Message.UpdateListItems(
                listItems = newListItems.toList()
            )
        )
    }

    private fun updateExtraEpisodesInfo(itemIndex: Int) {
        val listItem = state().sectionContent.listItems
            .getOrNull(itemIndex) ?: return

        updateExtraEpisodesInfoJobMap[listItem.id]?.cancel()
        updateExtraEpisodesInfoJobMap[listItem.id] = scope.launch {
            val result = usecases
                .fetchAnimeByIdUsecase
                .execute(listItem.id)

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
        val currentListItem = state().sectionContent.listItems
            .getOrNull(itemIndex) ?: return

        val newListItem = currentListItem.copy(
            extraEpisodesInfo = updateListItem.extraEpisodesInfo
        )
        val newListItems = state().sectionContent.listItems
            .toMutableList()

        newListItems[itemIndex] = newListItem
        dispatch(
            OngoingSectionStore.Message.UpdateListItems(
                listItems = newListItems.toList()
            )
        )
    }
}
