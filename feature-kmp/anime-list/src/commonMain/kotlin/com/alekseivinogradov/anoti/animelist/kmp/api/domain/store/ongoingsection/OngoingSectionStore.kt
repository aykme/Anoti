package com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.AnimeDetails
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionContentDomain
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.arkivanov.mvikotlin.core.store.Store

/**
 * The store for the "ongoing" section's list.
 */
interface OngoingSectionStore
    : Store<OngoingSectionStore.Intent, OngoingSectionStore.State, OngoingSectionStore.Label> {

    /** @param sectionContent the section's list content. */
    data class State(
        val sectionContent: SectionContentDomain = SectionContentDomain()
    )

    sealed interface Intent {
        /** The section became selected. */
        data object OpenSection : Intent

        /** Refresh the section's content type based on its current items. */
        data object UpdateSection : Intent

        /** The user scrolled to the end of the list. */
        data object LoadNextPage : Intent

        /** The user tapped the episode-info toggle on the item with [id]. */
        data class EpisodesInfoClick(val id: AnimeId) : Intent
    }

    sealed interface Label {
        /** Ask the main store to reset the list's scroll position. */
        data object ResetListPositionAfterUpdate : Label
    }

    /** Internal executor plumbing; a consumer never dispatches this. */
    sealed interface Action {
        /** The section is being opened for the first time; load its first page. */
        data object InitSection : Action
    }

    /** Internal executor plumbing; a consumer never dispatches this. */
    sealed interface Message {
        data class ChangeContentType(val contentType: ContentTypeDomain) : Message
        data class UpdateListItems(val listItems: List<ListItemDomain>) : Message
        data class UpdateEnabledExtraEpisodesInfoIds(
            val enabledExtraEpisodesInfoIds: Set<AnimeId>
        ) : Message

        data class UpdateAnimeDetails(val animeDetails: AnimeDetails) : Message
    }
}
