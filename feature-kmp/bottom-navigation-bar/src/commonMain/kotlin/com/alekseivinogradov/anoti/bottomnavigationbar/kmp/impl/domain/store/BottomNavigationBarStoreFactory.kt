package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.domain.store

import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

class BottomNavigationBarStoreFactory(
    private val storeFactory: StoreFactory
) {
    fun create(): BottomNavigationBarStore {
        return object :
            BottomNavigationBarStore,
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
