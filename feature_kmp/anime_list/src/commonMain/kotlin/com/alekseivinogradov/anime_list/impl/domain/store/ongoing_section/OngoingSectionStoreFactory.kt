package com.alekseivinogradov.anime_list.impl.domain.store.ongoing_section

import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.OngoingUsecases
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

internal class OngoingSectionStoreFactory(
    private val storeFactory: StoreFactory,
    ongoingUsecases: OngoingUsecases
) {

    val executorFactory: () -> OngoingSectionExecutor = {
        OngoingSectionExecutorImpl(usecases = ongoingUsecases)
    }

    internal fun create(): OngoingSectionStore {
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