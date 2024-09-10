package com.alekseivinogradov.anime_list.api.domain.store.ongoing_section

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

internal typealias OngoingSectionExecutor = CoroutineExecutor<
        OngoingSectionStore.Intent,
        OngoingSectionStore.Action,
        OngoingSectionStore.State,
        OngoingSectionStore.Message,
        OngoingSectionStore.Label
        >