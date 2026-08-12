package com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

internal typealias AnimeDatabaseExecutor = CoroutineExecutor<
    AnimeDatabaseStore.Intent,
    AnimeDatabaseStore.Action,
    AnimeDatabaseStore.State,
    AnimeDatabaseStore.Message,
    AnimeDatabaseStore.Label
    >
