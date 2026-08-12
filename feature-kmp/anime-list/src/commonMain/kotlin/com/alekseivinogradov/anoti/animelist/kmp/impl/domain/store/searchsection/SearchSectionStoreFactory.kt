package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.searchsection

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

class SearchSectionStoreFactory(
    private val storeFactory: StoreFactory,
    private val executorFactory: SearchSectionExecutorFactory,
) {

    fun create(): SearchSectionStore {
        return object :
            SearchSectionStore,
            Store<SearchSectionStore.Intent, SearchSectionStore.State, SearchSectionStore.Label>
            by storeFactory.create(
                name = "SearchSectionStore",
                initialState = SearchSectionStore.State(),
                bootstrapper = SimpleBootstrapper(),
                executorFactory = executorFactory,
                reducer = SearchSectionReducerImpl()
            ) {}
    }
}
