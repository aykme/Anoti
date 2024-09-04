package com.alekseivinogradov.anime_list.impl.domain.store.announced_section

import com.alekseivinogradov.anime_list.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.anime_list.api.domain.model.section.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.section.EpisodesInfoTypeDomain
import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeAnnouncedListUsecase
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private var updateSectionJob: Job? = null

internal class AnnouncedSectionExecutorImpl(
    private val fetchAnimeListUsecase: FetchAnimeAnnouncedListUsecase
) : AnnouncedSectionExecutor() {

    override fun executeIntent(intent: AnnouncedSectionStore.Intent) {
        when (intent) {
            AnnouncedSectionStore.Intent.InitSection -> initSection()
            AnnouncedSectionStore.Intent.UpdateSection -> updateSection()
            is AnnouncedSectionStore.Intent.EpisodesInfoClick -> episodeInfoClick(intent.itemIndex)
            is AnnouncedSectionStore.Intent.NotificationClick -> notificationClick()
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
                AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADING)
            )
            val result = fetchAnimeListUsecase.execute(
                page = 1,
                itemsPerPage = ITEMS_PER_PAGE
            )
            when (result) {
                is CallResult.Success -> {
                    dispatch(
                        AnnouncedSectionStore.Message.UpdateListItems(result.value)
                    )
                    if (result.value.isNotEmpty()) {
                        dispatch(
                            AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADED)
                        )
                    } else {
                        dispatch(
                            AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.NO_DATA)
                        )
                    }
                }

                is CallResult.HttpError,
                is CallResult.OtherError -> dispatch(
                    AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.NO_DATA)
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
                    AnnouncedSectionStore.Message.UpdateListItems(
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
                    AnnouncedSectionStore.Message.UpdateListItems(
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