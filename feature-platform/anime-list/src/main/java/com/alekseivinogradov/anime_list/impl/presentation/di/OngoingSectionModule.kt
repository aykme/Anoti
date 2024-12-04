package com.alekseivinogradov.anime_list.impl.presentation.di

import com.alekseivinogradov.anime_list.api.domain.source.AnimeListSource
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.impl.domain.store.ongoing_section.OngoingSectionExecutorFactory
import com.alekseivinogradov.anime_list.impl.domain.store.ongoing_section.OngoingSectionExecutorImpl
import com.alekseivinogradov.anime_list.impl.domain.store.ongoing_section.OngoingSectionStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchOngoingAnimeListUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.OngoingUsecases
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.api.domain.toast.provider.ToastProvider
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dagger.Module
import dagger.Provides

@Module
interface OngoingSectionModule {
    companion object {
        @Provides
        fun provideFetchOngoingAnimeListUsecase(
            source: AnimeListSource
        ): FetchOngoingAnimeListUsecase = FetchOngoingAnimeListUsecase(source)

        @Provides
        fun provideOngoingUsecases(
            fetchOngoingAnimeListUsecase: FetchOngoingAnimeListUsecase,
            fetchAnimeDetailsByIdUsecase: FetchAnimeDetailsByIdUsecase
        ): OngoingUsecases {
            return OngoingUsecases(
                fetchOngoingAnimeListUsecase = fetchOngoingAnimeListUsecase,
                fetchAnimeDetailsByIdUsecase = fetchAnimeDetailsByIdUsecase
            )
        }

        @Provides
        fun povideOngoingSectionExecutorFactory(
            coroutineContextProvider: CoroutineContextProvider,
            usecases: OngoingUsecases,
            toastProvider: ToastProvider
        ): OngoingSectionExecutorFactory = {
            OngoingSectionExecutorImpl(
                coroutineContextProvider = coroutineContextProvider,
                usecases = usecases,
                toastProvider = toastProvider
            )
        }

        @Provides
        fun provideOngoingSectionStore(
            storeFactory: StoreFactory,
            executorFactory: OngoingSectionExecutorFactory
        ): OngoingSectionStore {
            return OngoingSectionStoreFactory(
                storeFactory = storeFactory,
                executorFactory = executorFactory
            ).create()
        }
    }
}
