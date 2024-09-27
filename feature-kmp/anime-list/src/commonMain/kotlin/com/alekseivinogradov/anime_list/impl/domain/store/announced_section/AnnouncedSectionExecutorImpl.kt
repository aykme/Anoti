package com.alekseivinogradov.anime_list.impl.domain.store.announced_section

import com.alekseivinogradov.anime_list.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.EpisodesInfoTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.AnnouncedUsecases
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class AnnouncedSectionExecutorImpl(
    private val usecases: AnnouncedUsecases
) : AnnouncedSectionExecutor() {

    private var updateSectionJob: Job? = null

    override fun executeIntent(intent: AnnouncedSectionStore.Intent) {
        when (intent) {
            AnnouncedSectionStore.Intent.OpenSection -> initSection()
            AnnouncedSectionStore.Intent.UpdateSection -> updateSection()
            is AnnouncedSectionStore.Intent.EpisodesInfoClick -> episodeInfoClick(intent)
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
                AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.LOADING)
            )
            val result = usecases
                .fetchAnnouncedAnimeListUsecase
                .execute(page = 1)
            when (result) {
                is CallResult.Success -> {
                    dispatch(
                        AnnouncedSectionStore.Message.UpdateListItems(result.value)
                    )
                    if (result.value.isNotEmpty()) {
                        dispatch(
                            AnnouncedSectionStore.Message.ChangeContentType(
                                ContentTypeDomain.LOADED
                            )
                        )
                    } else {
                        dispatch(
                            AnnouncedSectionStore.Message.ChangeContentType(
                                ContentTypeDomain.NO_DATA
                            )
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

    private fun episodeInfoClick(intent: AnnouncedSectionStore.Intent.EpisodesInfoClick) {
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
            AnnouncedSectionStore.Message.UpdateListItems(
                listItems = newListItems.toList()
            )
        )
    }

    private fun availableEpisodesInfoClick(listItem: ListItemDomain, itemIndex: Int) {
        val newListItem = listItem.copy(
            episodesInfoType = EpisodesInfoTypeDomain.AVAILABLE
        )
        val newListItems = state().sectionContent.listItems
            .toMutableList()

        newListItems[itemIndex] = newListItem
        dispatch(
            AnnouncedSectionStore.Message.UpdateListItems(
                listItems = newListItems.toList()
            )
        )
    }
}
