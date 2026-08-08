package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.ongoingsection

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

class OngoingSectionStoreFactory(
    private val storeFactory: StoreFactory,
    private val executorFactory: OngoingSectionExecutorFactory
) {

    fun create(): OngoingSectionStore {
        return object : OngoingSectionStore,
            Store<OngoingSectionStore.Intent, OngoingSectionStore.State, OngoingSectionStore.Label>
            by storeFactory.create(
                name = "OngoingSectionStore",
                initialState = OngoingSectionStore.State(),
                bootstrapper = SimpleBootstrapper(OngoingSectionStore.Action.InitSection),
                executorFactory = executorFactory,
                reducer = OngoingSectionReducerImpl()
            ) {}
    }
}
