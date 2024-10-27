package com.alekseivinogradov.anime_list.impl.domain.store.search_section

import com.alekseivinogradov.anime_base.api.domain.ToastProvider
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.SearchUsecases
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

internal class SearchSectionStoreFactory(
    private val storeFactory: StoreFactory,
    coroutineContextProvider: CoroutineContextProvider,
    usecases: SearchUsecases,
    toastProvider: ToastProvider
) {
    private val executorFactory: () -> SearchSectionExecutor = {
        SearchSectionExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = usecases,
            toastProvider = toastProvider
        )
    }

    internal fun create(): SearchSectionStore {
        return object : SearchSectionStore,
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
