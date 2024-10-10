package com.alekseivinogradov.anime_favorites.impl.domain.store

import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.arkivanov.mvikotlin.core.store.Reducer

internal class AnimeFavoritesReducerImpl :
    Reducer<AnimeFavoritesMainStore.State, AnimeFavoritesMainStore.Message> {

    override fun AnimeFavoritesMainStore.State.reduce(msg: AnimeFavoritesMainStore.Message): AnimeFavoritesMainStore.State {
        return when (msg) {

            else -> AnimeFavoritesMainStore.State()
        }
    }
}
