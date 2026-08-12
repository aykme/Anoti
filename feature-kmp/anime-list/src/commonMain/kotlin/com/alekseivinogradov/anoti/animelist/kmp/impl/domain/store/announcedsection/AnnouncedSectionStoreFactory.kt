package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.announcedsection

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

class AnnouncedSectionStoreFactory(
    private val storeFactory: StoreFactory,
    private val executorFactory: AnnouncedSectionExecutorFactory
) {

    fun create(): AnnouncedSectionStore {
        return object :
            AnnouncedSectionStore,
            Store<
                AnnouncedSectionStore.Intent,
                AnnouncedSectionStore.State,
                AnnouncedSectionStore.Label
                >
            by storeFactory.create(
                name = "AnnouncedSectionStore",
                initialState = AnnouncedSectionStore.State(),
                bootstrapper = SimpleBootstrapper(),
                executorFactory = executorFactory,
                reducer = AnnouncedSectionReducerImpl()
            ) {}
    }
}
