package com.alekseivinogradov.anoti.infodialog.kmp.api.domain.store

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

internal typealias InfoDialogExecutor = CoroutineExecutor<
    InfoDialogStore.Intent,
    InfoDialogStore.Action,
    InfoDialogStore.State,
    InfoDialogStore.Message,
    InfoDialogStore.Label
    >
