package com.alekseivinogradov.anime_favorites.api.domain.store

import com.alekseivinogradov.anime_base.api.domain.AnimeId
import com.arkivanov.mvikotlin.core.store.Store

interface AnimeFavoritesMainStore :
    Store<AnimeFavoritesMainStore.Intent, AnimeFavoritesMainStore.State, AnimeFavoritesMainStore.Label> {

    class State

    sealed interface Intent {
        data object UpdateSection : Intent
        data class ItemClick(val id: AnimeId) : Intent
        data class InfoTypeClick(val id: AnimeId) : Intent
        data class NotificationClick(val id: AnimeId) : Intent
        data class EpisodesViewedMinusClick(val id: AnimeId) : Intent
        data class EpisodesViewedPlusClick(val id: AnimeId) : Intent
    }

    sealed interface Label

    sealed interface Action

    sealed interface Message
}