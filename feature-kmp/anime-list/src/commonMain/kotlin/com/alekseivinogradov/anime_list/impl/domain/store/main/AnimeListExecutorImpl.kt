package com.alekseivinogradov.anime_list.impl.domain.store.main

import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.SearchDomain
import com.alekseivinogradov.anime_list.api.domain.model.SectionHatDomain
import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListExecutor
import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListMainStore

internal class AnimeListExecutorImpl : AnimeListExecutor() {

    override fun executeIntent(intent: AnimeListMainStore.Intent) {
        when (intent) {
            AnimeListMainStore.Intent.OngoingsSectionClick -> ongoingSectionClick()
            AnimeListMainStore.Intent.AnnouncedSectionClick -> announcedSectionClick()
            AnimeListMainStore.Intent.SearchSectionClick -> searchSectionClick()
            AnimeListMainStore.Intent.CancelSearchClick -> cancelSearchClick()
            is AnimeListMainStore.Intent.ChangeSearchText -> changeSearchText(intent)
            is AnimeListMainStore.Intent.ChangeResetListPositionAfterUpdateStatus -> {
                changeResetListPositionAfterUpdateStatus(intent)
            }

            AnimeListMainStore.Intent.UpdateSection -> updateSection()
            is AnimeListMainStore.Intent.UpdateOngoingContent -> updateOngoingContent(intent)
            is AnimeListMainStore.Intent.UpdateAnnouncedContent -> updateAnnouncedContent(intent)
            is AnimeListMainStore.Intent.UpdateSearchContent -> updateSearchContent(intent)
            is AnimeListMainStore.Intent.EpisodesInfoClick -> episodeInfoClick(intent)
            is AnimeListMainStore.Intent.NotificationClick -> notificationClick(intent)
            is AnimeListMainStore.Intent.UpdateEnabledNotificationIds -> {
                updateEnabledNotificationIds(intent)
            }
        }
    }

    private fun ongoingSectionClick() {
        when (state().selectedSection) {
            SectionHatDomain.ONGOINGS -> Unit
            SectionHatDomain.ANNOUNCED,
            SectionHatDomain.SEARCH,
            -> {
                dispatch(
                    AnimeListMainStore.Message.ChangeSelectedSection(
                        selectedSection = SectionHatDomain.ONGOINGS
                    )
                )
                openOngoingSection()
            }
        }
    }

    private fun openOngoingSection() {
        publish(
            AnimeListMainStore.Label.OpenOngoingSection
        )
    }

    private fun announcedSectionClick() {
        when (state().selectedSection) {
            SectionHatDomain.ANNOUNCED -> Unit
            SectionHatDomain.ONGOINGS,
            SectionHatDomain.SEARCH,
            -> {
                dispatch(
                    AnimeListMainStore.Message.ChangeSelectedSection(
                        selectedSection = SectionHatDomain.ANNOUNCED
                    )
                )
                openAnnouncedSection()
            }
        }
    }

    private fun openAnnouncedSection() {
        publish(
            AnimeListMainStore.Label.OpenAnnouncedSection
        )
    }

    private fun searchSectionClick() {
        val state = state()
        when (state.selectedSection) {
            SectionHatDomain.SEARCH -> Unit
            SectionHatDomain.ONGOINGS,
            SectionHatDomain.ANNOUNCED,
            -> {
                dispatch(
                    AnimeListMainStore.Message.ChangeSelectedSection(
                        selectedSection = SectionHatDomain.SEARCH
                    )
                )
                openSearchSection()
            }
        }
        dispatch(
            AnimeListMainStore.Message.ChangeSearch(
                search = state.search.copy(
                    type = SearchDomain.Type.SHOWN
                )
            )
        )
    }

    private fun openSearchSection() {
        publish(
            AnimeListMainStore.Label.OpenSearchSection
        )
    }

    private fun cancelSearchClick() {
        dispatch(
            AnimeListMainStore.Message.ChangeSearch(
                search = state().search.copy(
                    type = SearchDomain.Type.HIDEN
                )
            )
        )
    }

    private fun changeSearchText(intent: AnimeListMainStore.Intent.ChangeSearchText) {
        dispatch(
            AnimeListMainStore.Message.ChangeSearch(
                search = state().search.copy(
                    searchText = intent.searchText
                )
            )
        )
        publish(
            AnimeListMainStore.Label.ChangeSearchText(
                searchText = state().search.searchText
            )
        )
    }

    private fun updateSection() {
        when (state().selectedSection) {
            SectionHatDomain.ONGOINGS -> publish(AnimeListMainStore.Label.UpdateOngoingSection)
            SectionHatDomain.ANNOUNCED -> publish(AnimeListMainStore.Label.UpdateAnnouncedSection)
            SectionHatDomain.SEARCH -> publish(AnimeListMainStore.Label.UpdateSearchSection)
        }
    }

    private fun updateOngoingContent(intent: AnimeListMainStore.Intent.UpdateOngoingContent) {
        val state = state()
        if (state.ongoingContent.contentType != intent.content.contentType) {
            dispatch(
                AnimeListMainStore.Message.ChangeOngoingContentType(intent.content.contentType)
            )
        }
        if (state.ongoingContent.listItems != intent.content.listItems) {
            dispatch(
                AnimeListMainStore.Message.UpdateOngoingListItems(intent.content.listItems)
            )
        }
        if (
            state.ongoingContent.enabledExtraEpisodesInfoIds !=
            intent.content.enabledExtraEpisodesInfoIds
        ) {
            dispatch(
                AnimeListMainStore.Message.UpdateOngoingEnabledExtraEpisodesInfoIds(
                    intent.content.enabledExtraEpisodesInfoIds
                )
            )
        }
        if (state.ongoingContent.animeDetails != intent.content.animeDetails) {
            dispatch(
                AnimeListMainStore.Message.UpdateOngoingAnimeDetails(
                    animeDetails = intent.content.animeDetails
                )
            )
        }
    }

    private fun updateAnnouncedContent(intent: AnimeListMainStore.Intent.UpdateAnnouncedContent) {
        val state = state()
        if (state.announcedContent.contentType != intent.content.contentType) {
            dispatch(
                AnimeListMainStore.Message.ChangeAnnouncedContentType(intent.content.contentType)
            )
        }
        if (state.announcedContent.listItems != intent.content.listItems) {
            dispatch(
                AnimeListMainStore.Message.UpdateAnnouncedListItems(intent.content.listItems)
            )
        }
        if (
            state.announcedContent.enabledExtraEpisodesInfoIds !=
            intent.content.enabledExtraEpisodesInfoIds
        ) {
            dispatch(
                AnimeListMainStore.Message.UpdateAnnouncedEnabledExtraEpisodesInfoIds(
                    intent.content.enabledExtraEpisodesInfoIds
                )
            )
        }
    }

    private fun changeResetListPositionAfterUpdateStatus(
        intent: AnimeListMainStore.Intent.ChangeResetListPositionAfterUpdateStatus
    ) {
        dispatch(
            AnimeListMainStore.Message.ChangeResetListPositionAfterUpdateStatus(
                isNeedToResetListPosition = intent.isNeedToResetListPosition
            )
        )
    }

    private fun updateSearchContent(intent: AnimeListMainStore.Intent.UpdateSearchContent) {
        val state = state()
        if (state.searchContent.contentType != intent.content.contentType) {
            dispatch(
                AnimeListMainStore.Message.ChangeSearchContentType(intent.content.contentType)
            )
        }
        if (state.searchContent.listItems != intent.content.listItems) {
            dispatch(
                AnimeListMainStore.Message.UpdateSearchListItems(intent.content.listItems)
            )
        }
        if (
            state.searchContent.enabledExtraEpisodesInfoIds !=
            intent.content.enabledExtraEpisodesInfoIds
        ) {
            dispatch(
                AnimeListMainStore.Message.UpdateSearchEnabledExtraEpisodesInfoIds(
                    intent.content.enabledExtraEpisodesInfoIds
                )
            )
        }
        if (state.searchContent.animeDetails != intent.content.animeDetails) {
            dispatch(
                AnimeListMainStore.Message.UpdateSearchAnimeDetails(
                    animeDetails = intent.content.animeDetails
                )
            )
        }
    }

    private fun episodeInfoClick(intent: AnimeListMainStore.Intent.EpisodesInfoClick) {
        when (state().selectedSection) {
            SectionHatDomain.ONGOINGS -> publish(
                AnimeListMainStore.Label.OngoingEpisodeInfoClick(intent.listItem)
            )

            SectionHatDomain.ANNOUNCED -> publish(
                AnimeListMainStore.Label.AnnouncedEpisodeInfoClick(intent.listItem)
            )

            SectionHatDomain.SEARCH -> publish(
                AnimeListMainStore.Label.SearchEpisodeInfoClick(intent.listItem)
            )
        }
    }

    private fun notificationClick(intent: AnimeListMainStore.Intent.NotificationClick) {
        if (state().enabledNotificationIds.contains(intent.listItem.id).not()) {
            enableNotification(intent.listItem)
        } else {
            disableNotification(intent.listItem.id)
        }
    }

    private fun enableNotification(listItem: ListItemDomain) {
        publish(AnimeListMainStore.Label.EnableNotificationClick(listItem))
    }

    private fun disableNotification(id: Int) {
        publish(AnimeListMainStore.Label.DisableNotificationClick(id))
    }

    private fun updateEnabledNotificationIds(
        intent: AnimeListMainStore.Intent.UpdateEnabledNotificationIds
    ) {
        dispatch(
            AnimeListMainStore.Message.UpdateEnabledNotificationIds(
                intent.enabledNotificationIds
            )
        )
    }
}
