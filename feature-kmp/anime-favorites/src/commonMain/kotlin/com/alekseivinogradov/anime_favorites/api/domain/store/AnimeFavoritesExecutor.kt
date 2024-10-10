package com.alekseivinogradov.anime_favorites.api.domain.store

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

internal typealias AnimeFavoritesExecutor = CoroutineExecutor<
        AnimeFavoritesMainStore.Intent,
        AnimeFavoritesMainStore.Action,
        AnimeFavoritesMainStore.State,
        AnimeFavoritesMainStore.Message,
        AnimeFavoritesMainStore.Label
        >
