package com.alekseivinogradov.anoti.infodialog.kmp.impl.domain.store

import com.alekseivinogradov.anoti.infodialog.kmp.api.domain.store.InfoDialogStore

/** Builds the [InfoDialogExecutorImpl] each [InfoDialogStore] runs. */
typealias InfoDialogExecutorFactory = () -> InfoDialogExecutorImpl
