package com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store

import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.arkivanov.mvikotlin.core.store.Store

/**
 * The store for the favorites screen.
 */
interface AnimeFavoritesMainStore :
    Store<
            AnimeFavoritesMainStore.Intent,
            AnimeFavoritesMainStore.State,
            AnimeFavoritesMainStore.Label
            > {

    /**
     * @param listItems favorites list items.
     * @param contentType loading state of the list.
     * @param enabledExtraInfoIds ids currently showing the extra episode-info variant.
     * @param fetchedAnimeDetailsIds ids whose extra details have already been fetched.
     */
    data class State(
        val listItems: List<ListItemDomain> = listOf(),
        val contentType: ContentTypeDomain = ContentTypeDomain.LOADING,
        val enabledExtraInfoIds: Set<AnimeId> = setOf(),
        val fetchedAnimeDetailsIds: Set<AnimeId> = setOf()
    )

    /** Actions a caller can dispatch via [accept]. */
    sealed interface Intent {
        /** Replace the list items with [listItems] (from the database store). */
        data class UpdateListItems(val listItems: List<ListItemDomain>) : Intent

        /** The list items were rendered at least once. */
        data object ItemsSubmittedToList : Intent

        /** Refresh the list's content type based on the current items. */
        data object UpdateSection : Intent

        /** Trigger a background update of every favorited anime. */
        data object UpdateAllItemsInBackground : Intent

        /** The user tapped the item with [id]. */
        data class ItemClick(val id: AnimeId) : Intent

        /** The user tapped the episode-info toggle on the item with [id]. */
        data class InfoTypeClick(val id: AnimeId) : Intent

        /** The user tapped the notification toggle on the item with [id]. */
        data class NotificationClick(val id: AnimeId) : Intent

        /** The user decremented viewed episodes for the item with [id]. */
        data class EpisodesViewedMinusClick(val id: AnimeId) : Intent

        /** The user incremented viewed episodes for the item with [id]. */
        data class EpisodesViewedPlusClick(val id: AnimeId) : Intent
    }

    /** One-off events the store publishes for callers to react to. */
    sealed interface Label {
        /** Ask the database store for a fresh section update. */
        data object UpdateSection : Label

        /** Navigate to the details of the item with [id]. */
        data class ItemClick(val id: AnimeId) : Label

        /** Disable the "new episode" notification for the item with [id]. */
        data class DisableNotificationClick(val id: AnimeId) : Label

        /** Persist [listItem]'s updated viewed-episodes count to the database. */
        data class UpdateListItem(val listItem: ListItemDomain) : Label
    }

    /** Internal executor plumbing; a consumer never dispatches this. */
    sealed interface Action

    /** Internal executor plumbing; a consumer never dispatches this. */
    sealed interface Message {
        /**
         * Replaces [State.listItems] wholesale.
         *
         * @param listItems the full, up-to-date list of favorites items.
         */
        data class UpdateListItems(val listItems: List<ListItemDomain>) : Message

        /**
         * Replaces [State.contentType].
         *
         * @param contentType the list's new loading state.
         */
        data class ChangeContentType(val contentType: ContentTypeDomain) : Message

        /**
         * Replaces [State.enabledExtraInfoIds] wholesale.
         *
         * @param enabledExtraInfoIds ids that should show the extra episode-info variant.
         */
        data class UpdateEnabledExtraInfoIds(val enabledExtraInfoIds: Set<AnimeId>) : Message

        /**
         * Replaces [State.fetchedAnimeDetailsIds] wholesale.
         *
         * @param fetchedAnimeDetailsIds ids whose extra details have been fetched.
         */
        data class UpdateFetchedAnimeDetailsIds(val fetchedAnimeDetailsIds: Set<AnimeId>) : Message
    }
}
