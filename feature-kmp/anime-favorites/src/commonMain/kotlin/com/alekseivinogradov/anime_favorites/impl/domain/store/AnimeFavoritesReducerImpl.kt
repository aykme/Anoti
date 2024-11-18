package com.alekseivinogradov.anime_favorites.impl.domain.store

import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.arkivanov.mvikotlin.core.store.Reducer

internal class AnimeFavoritesReducerImpl :
    Reducer<AnimeFavoritesMainStore.State, AnimeFavoritesMainStore.Message> {

    override fun AnimeFavoritesMainStore.State.reduce(
        msg: AnimeFavoritesMainStore.Message
    ): AnimeFavoritesMainStore.State {
        return when (msg) {
            is AnimeFavoritesMainStore.Message.UpdateListItems -> copy(
                listItems = msg.listItems
            )

            is AnimeFavoritesMainStore.Message.ChangeContentType -> copy(
                contentType = msg.contentType
            )

            is AnimeFavoritesMainStore.Message.UpdateEnabledExtraInfoIds -> copy(
                enabledExtraInfoIds = msg.enabledExtraInfoIds
            )

            is AnimeFavoritesMainStore.Message.UpdateFetchedAnimeDetailsIds -> copy(
                fetchedAnimeDetailsIds = msg.fetchedAnimeDetailsIds
            )
        }
    }
}
