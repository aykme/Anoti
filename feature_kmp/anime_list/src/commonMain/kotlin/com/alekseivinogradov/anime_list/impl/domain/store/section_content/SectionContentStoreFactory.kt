package com.alekseivinogradov.anime_list.impl.domain.store.section_content

import com.alekseivinogradov.anime_list.api.domain.store.section_content.SectionContentStore
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

internal class SectionContentStoreFactory(private val storeFactory: StoreFactory) {

    internal fun create(): SectionContentStore {
        return object : SectionContentStore,
            Store<SectionContentStore.Intent, SectionContentStore.State, SectionContentStore.Label>
            by storeFactory.create(
                name = "SectionContentStore",
                initialState = SectionContentStore.State(),
                bootstrapper = SimpleBootstrapper(),
                executorFactory = ::SectionContentExecutorImpl,
                reducer = SectionContentReducerImpl()
            ) {}
    }
}