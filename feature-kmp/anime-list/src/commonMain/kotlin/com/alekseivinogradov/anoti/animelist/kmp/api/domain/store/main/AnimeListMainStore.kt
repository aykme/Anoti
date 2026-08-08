package com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.AnimeDetails
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SearchDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionContentDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionHatDomain
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.arkivanov.mvikotlin.core.store.Store

/**
 * The top-level store for the anime list screen: selected section, search bar, and the
 * notification-enabled ids shared across sections.
 */
interface AnimeListMainStore
    : Store<AnimeListMainStore.Intent, AnimeListMainStore.State, AnimeListMainStore.Label> {

    /**
     * @param selectedSection currently selected section.
     * @param search state of the search bar.
     * @param ongoingContent ongoing section's list content, mirrored from its own store.
     * @param announcedContent announced section's list content, mirrored from its own store.
     * @param searchContent search section's list content, mirrored from its own store.
     * @param enabledNotificationIds ids with the "new episode" notification enabled.
     * @param isNeedToResetListPositon whether the list's scroll position should reset.
     */
    data class State(
        val selectedSection: SectionHatDomain = SectionHatDomain.ONGOINGS,
        val search: SearchDomain = SearchDomain(),
        val ongoingContent: SectionContentDomain = SectionContentDomain(),
        val announcedContent: SectionContentDomain = SectionContentDomain(),
        val searchContent: SectionContentDomain = SectionContentDomain(),
        val enabledNotificationIds: Set<AnimeId> = setOf(),
        val isNeedToResetListPositon: Boolean = false
    )

    sealed interface Intent {
        /** The user tapped the ongoing tab. */
        data object OngoingsSectionClick : Intent

        /** The user tapped the announced tab. */
        data object AnnouncedSectionClick : Intent

        /** The user tapped the search tab. */
        data object SearchSectionClick : Intent

        /** The user closed the search bar. */
        data object CancelSearchClick : Intent

        /** The user edited the search text to [searchText]. */
        data class ChangeSearchText(val searchText: String) : Intent

        /** Set the reset-scroll-position flag to [isNeedToResetListPosition]. */
        data class ChangeResetListPositionFlag(val isNeedToResetListPosition: Boolean) : Intent

        /** Refresh the selected section's content type based on its current items. */
        data object UpdateSection : Intent

        /** Replace the ongoing section's content with [content] (mirrored from its store). */
        data class UpdateOngoingContent(val content: SectionContentDomain) : Intent

        /** Replace the announced section's content with [content] (mirrored from its store). */
        data class UpdateAnnouncedContent(val content: SectionContentDomain) : Intent

        /** Replace the search section's content with [content] (mirrored from its store). */
        data class UpdateSearchContent(val content: SectionContentDomain) : Intent

        /** The user tapped the episode-info toggle on the item with [id]. */
        data class EpisodesInfoClick(val id: AnimeId) : Intent

        /** The user tapped the notification toggle on the item with [id]. */
        data class NotificationClick(val id: AnimeId) : Intent

        /** Replace the set of notification-enabled ids with [enabledNotificationIds]. */
        data class UpdateEnabledNotificationIds(val enabledNotificationIds: Set<AnimeId>) : Intent

        /** The user scrolled to the end of the selected section's list. */
        data object LoadNextPage : Intent
    }

    sealed interface Label {
        /** Ask the ongoing section's store for a section update. */
        data object OpenOngoingSection : Label

        /** Ask the announced section's store for a section update. */
        data object OpenAnnouncedSection : Label

        /** Ask the search section's store for a section update. */
        data object OpenSearchSection : Label

        /** Ask the ongoing section's store to refresh its content. */
        data object UpdateOngoingSection : Label

        /** Ask the announced section's store to refresh its content. */
        data object UpdateAnnouncedSection : Label

        /** Ask the search section's store to refresh its content. */
        data object UpdateSearchSection : Label

        /** Ask the ongoing section's store to load its next page. */
        data object LoadNextPageOngoingSection : Label

        /** Ask the announced section's store to load its next page. */
        data object LoadNextPageAnnouncedSection : Label

        /** Ask the search section's store to load its next page. */
        data object LoadNextPageSearchSection : Label

        /** Forward the changed search text to the search section's store. */
        data class ChangeSearchText(val searchText: String) : Label

        /** Forward an episode-info toggle for the item with [id] to the ongoing section's store. */
        data class OngoingEpisodeInfoClick(val id: AnimeId) : Label

        /**
         * Forward an episode-info toggle for the item with [id] to the announced section's
         * store.
         */
        data class AnnouncedEpisodeInfoClick(val id: AnimeId) : Label

        /** Forward an episode-info toggle for the item with [id] to the search section's store. */
        data class SearchEpisodeInfoClick(val id: AnimeId) : Label

        /** Persist the "new episode" notification as enabled for [listItem]. */
        data class EnableNotificationClick(val listItem: ListItemDomain) : Label

        /** Persist the "new episode" notification as disabled for the item with [id]. */
        data class DisableNotificationClick(val id: AnimeId) : Label
    }

    /** Internal executor plumbing; a consumer never dispatches this. */
    sealed interface Action

    /** Internal executor plumbing; a consumer never dispatches this. */
    sealed interface Message {
        data class ChangeSelectedSection(val selectedSection: SectionHatDomain) : Message
        data class ChangeSearch(val search: SearchDomain) : Message
        data class ChangeOngoingContentType(val contentType: ContentTypeDomain) : Message
        data class ChangeAnnouncedContentType(val contentType: ContentTypeDomain) : Message
        data class ChangeSearchContentType(val contentType: ContentTypeDomain) : Message
        data class ChangeResetListPositionFlag(val isNeedToResetListPosition: Boolean) : Message
        data class UpdateOngoingListItems(val listItems: List<ListItemDomain>) : Message
        data class UpdateAnnouncedListItems(val listItems: List<ListItemDomain>) : Message
        data class UpdateSearchListItems(val listItems: List<ListItemDomain>) : Message
        data class UpdateEnabledNotificationIds(val enabledNotificationIds: Set<AnimeId>) : Message
        data class UpdateOngoingEnabledExtraEpisodesInfoIds(
            val enabledExtraEpisodesInfoId: Set<AnimeId>
        ) : Message

        data class UpdateAnnouncedEnabledExtraEpisodesInfoIds(
            val enabledExtraEpisodesInfoId: Set<AnimeId>
        ) : Message

        data class UpdateSearchEnabledExtraEpisodesInfoIds(
            val enabledExtraEpisodesInfoId: Set<AnimeId>
        ) : Message

        data class UpdateOngoingAnimeDetails(
            val animeDetails: AnimeDetails
        ) : Message

        data class UpdateSearchAnimeDetails(
            val animeDetails: AnimeDetails
        ) : Message
    }
}
