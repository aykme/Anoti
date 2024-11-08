package com.alekseivinogradov.anime_list.impl.domain.store.announced_section

import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

class AnnouncedSectionStoreFactory(
    private val storeFactory: StoreFactory,
    private val executorFactory: () -> AnnouncedSectionExecutorImpl
) {

    fun create(): AnnouncedSectionStore {
        return object : AnnouncedSectionStore,
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
