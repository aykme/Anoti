package com.alekseivinogradov.anoti.infodialog.kmp.impl.domain.store

import com.alekseivinogradov.anoti.infodialog.kmp.api.domain.store.InfoDialogStore
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

/** Builds an [InfoDialogStore] through the app's [StoreFactory]. */
class InfoDialogStoreFactory(
    private val storeFactory: StoreFactory,
    private val executorFactory: InfoDialogExecutorFactory
) {
    /** A new store, hidden; nothing runs until an intent arrives. */
    fun create(): InfoDialogStore = object :
        InfoDialogStore,
        Store<InfoDialogStore.Intent, InfoDialogStore.State, InfoDialogStore.Label>
        by storeFactory.create(
            name = "InfoDialogStore",
            initialState = InfoDialogStore.State(),
            bootstrapper = SimpleBootstrapper(),
            executorFactory = executorFactory,
            reducer = InfoDialogReducerImpl()
        ) {}
}
