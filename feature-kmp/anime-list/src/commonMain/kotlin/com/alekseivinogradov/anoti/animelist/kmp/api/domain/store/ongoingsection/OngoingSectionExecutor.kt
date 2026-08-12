package com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

internal typealias OngoingSectionExecutor = CoroutineExecutor<
    OngoingSectionStore.Intent,
    OngoingSectionStore.Action,
    OngoingSectionStore.State,
    OngoingSectionStore.Message,
    OngoingSectionStore.Label
    >
