package com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

internal typealias AnimeFavoritesExecutor = CoroutineExecutor<
        AnimeFavoritesMainStore.Intent,
        AnimeFavoritesMainStore.Action,
        AnimeFavoritesMainStore.State,
        AnimeFavoritesMainStore.Message,
        AnimeFavoritesMainStore.Label
        >
