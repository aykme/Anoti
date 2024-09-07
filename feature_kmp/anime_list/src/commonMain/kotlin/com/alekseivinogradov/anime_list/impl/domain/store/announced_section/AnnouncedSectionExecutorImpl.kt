package com.alekseivinogradov.anime_list.impl.domain.store.announced_section

import com.alekseivinogradov.anime_list.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.anime_list.api.domain.model.section.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.section.EpisodesInfoTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.section.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeAnnouncedListUsecase
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class AnnouncedSectionExecutorImpl(
    private val fetchAnimeListUsecase: FetchAnimeAnnouncedListUsecase
) : AnnouncedSectionExecutor() {

    private var updateSectionJob: Job? = null

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
            AnnouncedSectionStore.Message.UpdateListItems(
                listItems = newListItems.toList()
            )
        )
    }

    private fun availableEpisodesInfoClick(listItem: ListItemDomain, itemIndex: Int) {
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

    private fun notificationClick() {

    }
}