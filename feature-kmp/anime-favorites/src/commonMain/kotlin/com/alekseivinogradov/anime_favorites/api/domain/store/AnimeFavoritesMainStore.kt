package com.alekseivinogradov.anime_favorites.api.domain.store

import com.alekseivinogradov.anime_base.api.domain.AnimeId
import com.alekseivinogradov.anime_favorites.api.domain.model.ListItemDomain
import com.arkivanov.mvikotlin.core.store.Store

interface AnimeFavoritesMainStore :
    Store<AnimeFavoritesMainStore.Intent, AnimeFavoritesMainStore.State, AnimeFavoritesMainStore.Label> {

    data class State(
        val listItems: List<ListItemDomain> = listOf(),
        val enabledExtraInfoIds: Set<AnimeId> = setOf(),
        val nextEpisodesInfo: Map<AnimeId, String> = mapOf()
    )

    sealed interface Intent {
        data class UpdateListItems(val listItems: List<ListItemDomain>) : Intent
        data object UpdateSection : Intent
        data class ItemClick(val id: AnimeId) : Intent
        data class InfoTypeClick(val id: AnimeId) : Intent
        data class NotificationClick(val id: AnimeId) : Intent
        data class EpisodesViewedMinusClick(val id: AnimeId) : Intent
        data class EpisodesViewedPlusClick(val id: AnimeId) : Intent
    }

    sealed interface Label {
        data object UpdateSection : Label
        data class ItemClick(val id: AnimeId) : Label
    }

    sealed interface Action

    sealed interface Message {
        data class UpdateListItems(val listItems: List<ListItemDomain>) : Message
    }
}
