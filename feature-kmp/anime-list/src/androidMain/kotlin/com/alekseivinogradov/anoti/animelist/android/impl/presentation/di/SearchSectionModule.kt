package com.alekseivinogradov.anoti.animelist.android.impl.presentation.di

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.searchsection.SearchSectionExecutorFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.searchsection.SearchSectionExecutorImpl
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.searchsection.SearchSectionStoreFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnimeListBySearchUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper.SearchUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
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
        fun provideSearchSectionExecutorFactory(
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
