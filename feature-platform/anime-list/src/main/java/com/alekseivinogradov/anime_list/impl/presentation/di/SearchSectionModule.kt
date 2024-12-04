package com.alekseivinogradov.anime_list.impl.presentation.di

import com.alekseivinogradov.anime_list.api.domain.source.AnimeListSource
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore
import com.alekseivinogradov.anime_list.impl.domain.store.search_section.SearchSectionExecutorFactory
import com.alekseivinogradov.anime_list.impl.domain.store.search_section.SearchSectionExecutorImpl
import com.alekseivinogradov.anime_list.impl.domain.store.search_section.SearchSectionStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeListBySearchUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.SearchUsecases
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.api.domain.toast.provider.ToastProvider
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dagger.Module
import dagger.Provides

@Module
interface SearchSectionModule {
    companion object {
        @Provides
        fun provideFetchAnimeListBySearchUsecase(
            source: AnimeListSource
        ): FetchAnimeListBySearchUsecase = FetchAnimeListBySearchUsecase(source)

        @Provides
        fun provideSearchUsecases(
            fetchAnimeListBySearchUsecase: FetchAnimeListBySearchUsecase,
            fetchAnimeDetailsByIdUsecase: FetchAnimeDetailsByIdUsecase
        ): SearchUsecases {
            return SearchUsecases(
                fetchAnimeListBySearchUsecase = fetchAnimeListBySearchUsecase,
                fetchAnimeDetailsByIdUsecase = fetchAnimeDetailsByIdUsecase
            )
        }

        @Provides
        fun povideSearchSectionExecutorFactory(
            coroutineContextProvider: CoroutineContextProvider,
            usecases: SearchUsecases,
            toastProvider: ToastProvider
        ): SearchSectionExecutorFactory = {
            SearchSectionExecutorImpl(
                coroutineContextProvider = coroutineContextProvider,
                usecases = usecases,
                toastProvider = toastProvider
            )
        }

        @Provides
        fun provideSearchSectionStore(
            storeFactory: StoreFactory,
            executorFactory: SearchSectionExecutorFactory
        ): SearchSectionStore {
            return SearchSectionStoreFactory(
                storeFactory = storeFactory,
                executorFactory = executorFactory
            ).create()
        }
    }
}
