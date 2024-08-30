package com.alekseivinogradov.anime_list.api.domain.store.upper_menu

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

internal typealias UpperMenuExecutor = CoroutineExecutor<
        UpperMenuStore.Intent,
        UpperMenuStore.Action,
        UpperMenuStore.State,
        UpperMenuStore.Message,
        UpperMenuStore.Label
        >