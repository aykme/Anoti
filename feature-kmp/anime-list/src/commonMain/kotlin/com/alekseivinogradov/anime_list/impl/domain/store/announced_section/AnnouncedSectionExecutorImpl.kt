package com.alekseivinogradov.anime_list.impl.domain.store.announced_section

import com.alekseivinogradov.anime_base.api.domain.AnimeId
import com.alekseivinogradov.anime_list.api.domain.model.ContentTypeDomain
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
                                ContentTypeDomain.ERROR
                            )
                        )
                    }
                }

                is CallResult.HttpError,
                is CallResult.OtherError -> dispatch(
                    AnnouncedSectionStore.Message.ChangeContentType(ContentTypeDomain.ERROR)
                )
            }
            dispatch(
                AnnouncedSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                    enabledExtraEpisodesInfoId = setOf()
                )
            )
        }
    }

    private fun episodeInfoClick(intent: AnnouncedSectionStore.Intent.EpisodesInfoClick) {
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
            AnnouncedSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                newEnabledExtraEpisodesInfoIds
            )
        )
    }

    private fun availableEpisodesInfoClick(listItem: ListItemDomain) {
        val newEnabledExtraEpisodesInfoIds = mutableSetOf<AnimeId>().apply {
            addAll(state().sectionContent.enabledExtraEpisodesInfoIds)
            remove(listItem.id)
        }.toSet()
        dispatch(
            AnnouncedSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds(
                newEnabledExtraEpisodesInfoIds
            )
        )
    }
}
