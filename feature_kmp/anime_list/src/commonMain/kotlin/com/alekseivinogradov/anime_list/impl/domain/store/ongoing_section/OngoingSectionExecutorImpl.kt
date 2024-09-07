package com.alekseivinogradov.anime_list.impl.domain.store.ongoing_section

import com.alekseivinogradov.anime_list.api.domain.AnimeId
import com.alekseivinogradov.anime_list.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.anime_list.api.domain.model.section.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.section.EpisodesInfoTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.section.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.section.ReleaseStatusDomain
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeByIdUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeOngoingListUsecase
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class OngoingSectionExecutorImpl(
    private val fetchAnimeListUsecase: FetchAnimeOngoingListUsecase,
    private val fetchAnimeByIdUsecase: FetchAnimeByIdUsecase
) : OngoingSectionExecutor() {

    private var updateSectionJob: Job? = null
    private val updateExtraEpisodesInfoJobMap = mutableMapOf<AnimeId, Job>()

    override fun executeIntent(intent: OngoingSectionStore.Intent) {
        when (intent) {
            OngoingSectionStore.Intent.InitSection -> initSection()
            OngoingSectionStore.Intent.UpdateSection -> updateSection()
            is OngoingSectionStore.Intent.EpisodesInfoClick -> episodeInfoClick(intent.itemIndex)
            is OngoingSectionStore.Intent.NotificationClick -> notificationClick()
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
                OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADING)
            )
            val result = fetchAnimeListUsecase.execute(
                page = 1,
                itemsPerPage = ITEMS_PER_PAGE
            )
            when (result) {
                is CallResult.Success -> {
                    dispatch(
                        OngoingSectionStore.Message.UpdateListItems(result.value)
                    )
                    if (result.value.isNotEmpty()) {
                        dispatch(
                            OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADED)
                        )
                    } else {
                        dispatch(
                            OngoingSectionStore.Message.ChangeContentType(ContentTypeDomain.NO_DATA)
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
        val newListItems = state().listItems.toMutableList()
        newListItems[itemIndex] = newListItem
        dispatch(
            OngoingSectionStore.Message.UpdateListItems(
                listItems = newListItems.toList()
            )
        )
    }

    private fun updateExtraEpisodesInfo(itemIndex: Int) {
        val listItem = state().listItems.getOrNull(itemIndex) ?: return
        val id = listItem.id ?: return

        updateExtraEpisodesInfoJobMap[id]?.cancel()
        updateExtraEpisodesInfoJobMap[id] = scope.launch {
            val result = fetchAnimeByIdUsecase.execute(id)
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
            OngoingSectionStore.Message.UpdateListItems(
                listItems = newListItems.toList()
            )
        )
    }

    private fun notificationClick() {

    }
}