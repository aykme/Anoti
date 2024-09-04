package com.alekseivinogradov.anime_list.impl.domain.store.ongoing_section

import com.alekseivinogradov.anime_list.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.anime_list.api.domain.model.section.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.section.EpisodesInfoTypeDomain
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeOngoingListUsecase
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private var updateSectionJob: Job? = null

internal class OngoingSectionExecutorImpl(
    private val fetchAnimeListUsecase: FetchAnimeOngoingListUsecase
) : OngoingSectionExecutor() {

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
        val listItem = state().listItems.getOrNull(itemIndex)
        when (listItem?.episodesInfoType) {
            EpisodesInfoTypeDomain.AVAILABLE -> {
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
            }

            EpisodesInfoTypeDomain.EXTRA -> {
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

            null -> Unit
        }
    }

    private fun notificationClick() {

    }
}