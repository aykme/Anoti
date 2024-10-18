package com.alekseivinogradov.anime_favorites.impl.domain.store

import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesExecutor
import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore

internal class AnimeFavoritesExecutorImpl() : AnimeFavoritesExecutor() {
    override fun executeIntent(intent: AnimeFavoritesMainStore.Intent) {
        when (intent) {
            is AnimeFavoritesMainStore.Intent.UpdateListItems -> updateListItems(intent)
            AnimeFavoritesMainStore.Intent.UpdateSection -> updateSection()
            is AnimeFavoritesMainStore.Intent.ItemClick -> itemClick(intent)
            is AnimeFavoritesMainStore.Intent.InfoTypeClick -> infoTypeClick(intent)
            is AnimeFavoritesMainStore.Intent.NotificationClick -> notificationClick(intent)
            is AnimeFavoritesMainStore.Intent.EpisodesViewedMinusClick -> {
                episodesViewedMinusClick(intent)
            }

            is AnimeFavoritesMainStore.Intent.EpisodesViewedPlusClick -> {
                episodesViewedPlusClick(intent)
            }
        }
    }

    private fun updateListItems(intent: AnimeFavoritesMainStore.Intent.UpdateListItems) {
        dispatch(AnimeFavoritesMainStore.Message.UpdateListItems(intent.listItems))
    }

    private fun updateSection() {

        println("tagtag updateSection")
    }

    private fun itemClick(intent: AnimeFavoritesMainStore.Intent.ItemClick) {
        println("tagtag itemClick: ${intent.id}")
    }

    private fun infoTypeClick(intent: AnimeFavoritesMainStore.Intent.InfoTypeClick) {
        println("tagtag infoTypeClick: ${intent.id}")
    }

    private fun notificationClick(intent: AnimeFavoritesMainStore.Intent.NotificationClick) {
        println("tagtag notificationClick: ${intent.id}")
    }

    private fun episodesViewedMinusClick(
        intent: AnimeFavoritesMainStore.Intent.EpisodesViewedMinusClick
    ) {
        println("tagtag episodesViewedMinusClick: ${intent.id}")
    }

    private fun episodesViewedPlusClick(
        intent: AnimeFavoritesMainStore.Intent.EpisodesViewedPlusClick
    ) {
        println("tagtag episodesViewedPlusClick: ${intent.id}")
    }
}
