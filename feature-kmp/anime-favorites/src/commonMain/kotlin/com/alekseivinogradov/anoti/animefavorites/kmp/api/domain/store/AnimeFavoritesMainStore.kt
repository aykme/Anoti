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
     * @param fetchedAnimeDetailsIds ids whose next-episode info was already fetched this
     * section load. A null [ListItemDomain.nextEpisodeAt] can mean either "not fetched yet" or
     * "fetched, and the API legitimately has none" — this id set is the only reliable way to
     * tell those apart and avoid re-fetching in the second case.
     */
    data class State(
        val listItems: List<ListItemDomain> = listOf(),
        val contentType: ContentTypeDomain = ContentTypeDomain.LOADING(),
        val fetchedAnimeDetailsIds: Set<AnimeId> = setOf()
    )

    /** Actions a caller can dispatch via [accept]. */
    sealed interface Intent {
        /** Replace the list items with [listItems] (from the database store). */
        data class UpdateListItems(val listItems: List<ListItemDomain>) : Intent

        /** The list items were rendered at least once. */
        data object ItemsSubmittedToList : Intent

        /**
         * The section became selected. Unlike [UpdateSection], never dispatched while already
         * restored from a process death that happened on this same section — see
         * `NavAnimeFavoritesScreenComponent`.
         */
        data object OpenSection : Intent

        /** Refresh the list's content type based on the current items. */
        data object UpdateSection : Intent

        /** Trigger a background update of every favorite anime. */
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

        /**
         * Ask the database store to turn off every item's extra-info display mode and clear its
         * next-episode date.
         */
        data object ResetExtraInfo : Label

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

        /** Replaces [State.fetchedAnimeDetailsIds]. */
        data class UpdateFetchedAnimeDetailsIds(
            val fetchedAnimeDetailsIds: Set<AnimeId>
        ) : Message
    }
}
