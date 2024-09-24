package com.alekseivinogradov.database.api.domain.store

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor


internal typealias DatabaseExecutor = CoroutineExecutor<
        DatabaseStore.Intent,
        DatabaseStore.Action,
        DatabaseStore.State,
        DatabaseStore.Message,
        DatabaseStore.Label
        >