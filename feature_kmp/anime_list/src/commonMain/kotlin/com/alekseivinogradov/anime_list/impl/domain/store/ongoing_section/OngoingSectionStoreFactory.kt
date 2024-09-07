package com.alekseivinogradov.anime_list.impl.domain.store.ongoing_section

import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeByIdUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeOngoingListUsecase
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

internal class OngoingSectionStoreFactory(
    private val storeFactory: StoreFactory,
    private val fetchAnimeListUsecase: FetchAnimeOngoingListUsecase,
    private val fetchAnimeByIdUsecase: FetchAnimeByIdUsecase
) {

    val executorFactory: () -> OngoingSectionExecutor = {
        OngoingSectionExecutorImpl(
            fetchAnimeListUsecase = fetchAnimeListUsecase,
            fetchAnimeByIdUsecase = fetchAnimeByIdUsecase
        )
    }

    internal fun create(): OngoingSectionStore {
        return object : OngoingSectionStore,
            Store<OngoingSectionStore.Intent, OngoingSectionStore.State, OngoingSectionStore.Label>
            by storeFactory.create(
                name = "OngoingSectionStore",
                initialState = OngoingSectionStore.State(),
                bootstrapper = SimpleBootstrapper(),
                executorFactory = executorFactory,
                reducer = OngoingSectionReducerImpl()
            ) {}
    }
}