package com.alekseivinogradov.anime_list.impl.domain.store.search_section

import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeByIdUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeListBySearchUsecase
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

internal class SearchSectionStoreFactory(
    private val storeFactory: StoreFactory,
    private val fetchAnimeListUsecase: FetchAnimeListBySearchUsecase,
    private val fetchAnimeByIdUsecase: FetchAnimeByIdUsecase
) {

    val executorFactory: () -> SearchSectionExecutor = {
        SearchSectionExecutorImpl(
            fetchAnimeListUsecase = fetchAnimeListUsecase,
            fetchAnimeByIdUsecase = fetchAnimeByIdUsecase
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