package com.alekseivinogradov.anime_list.impl.domain.store.upper_menu

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

internal class UpperMenuStoreFactory(private val storeFactory: StoreFactory) {

    internal fun create(): UpperMenuStore {
        return object : UpperMenuStore,
            Store<UpperMenuStore.Intent, UpperMenuStore.State, UpperMenuStore.Label>
            by storeFactory.create(
                name = "UpperMenuStore",
                initialState = UpperMenuStore.State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = ::UpperMenuExecutorImpl,
                reducer = UpperMenuReducer()
            ) {}
    }
}