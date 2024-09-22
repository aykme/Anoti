package com.alekseivinogradov.anime_list.api.domain.store.main

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

internal typealias AnimeListExecutor = CoroutineExecutor<
        AnimeListMainStore.Intent,
        AnimeListMainStore.Action,
        AnimeListMainStore.State,
        AnimeListMainStore.Message,
        AnimeListMainStore.Label
        >