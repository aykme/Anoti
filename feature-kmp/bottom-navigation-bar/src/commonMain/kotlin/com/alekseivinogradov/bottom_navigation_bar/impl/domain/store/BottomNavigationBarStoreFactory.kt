package com.alekseivinogradov.bottom_navigation_bar.impl.domain.store

import com.alekseivinogradov.bottom_navigation_bar.api.domain.store.BottomNavigationBarStore
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

internal class BottomNavigationBarStoreFactory(
    private val storeFactory: StoreFactory
) {
    internal fun create(): BottomNavigationBarStore {
        return object : BottomNavigationBarStore,
            Store<
                    BottomNavigationBarStore.Intent,
                    BottomNavigationBarStore.State,
                    BottomNavigationBarStore.Label
                    >
            by storeFactory.create(
                name = "BottomNavigationBarStore",
                initialState = BottomNavigationBarStore.State(),
                bootstrapper = SimpleBootstrapper(),
                executorFactory = ::BottomNavigationBarExecutorImpl,
                reducer = BottomNavigationBarReducerImpl()
            ) {}
    }
}
