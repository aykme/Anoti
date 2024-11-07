package com.alekseivinogradov.anime_favorites.api.domain.store

import com.alekseivinogradov.anime_favorites.api.domain.model.ListItemDomain
import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.arkivanov.mvikotlin.core.store.Store

interface AnimeFavoritesMainStore :
    Store<AnimeFavoritesMainStore.Intent, AnimeFavoritesMainStore.State, AnimeFavoritesMainStore.Label> {

    data class State(
        val listItems: List<ListItemDomain> = listOf(),
        val enabledExtraInfoIds: Set<AnimeId> = setOf(),
        val fetchedAnimeDetailsIds: Set<AnimeId> = setOf()
    )

    sealed interface Intent {
        data class UpdateListItems(val listItems: List<ListItemDomain>) : Intent
        data object UpdateSection : Intent
        data object UpdateAllItemsInBackground : Intent
        data class ItemClick(val id: AnimeId) : Intent
        data class InfoTypeClick(val id: AnimeId) : Intent
        data class NotificationClick(val id: AnimeId) : Intent
        data class EpisodesViewedMinusClick(val id: AnimeId) : Intent
        data class EpisodesViewedPlusClick(val id: AnimeId) : Intent
    }

    sealed interface Label {
        data object UpdateSection : Label
        data class ItemClick(val id: AnimeId) : Label
        data class DisableNotificationClick(val id: AnimeId) : Label
        data class UpdateListItem(val listItem: ListItemDomain) : Label
    }

    sealed interface Action

    sealed interface Message {
        data class UpdateListItems(val listItems: List<ListItemDomain>) : Message
        data class UpdateEnabledExtraInfoIds(val enabledExtraInfoIds: Set<AnimeId>) : Message
        data class UpdateFetchedAnimeDetailsIds(val fetchedAnimeDetailsIds: Set<AnimeId>) : Message
    }
}
