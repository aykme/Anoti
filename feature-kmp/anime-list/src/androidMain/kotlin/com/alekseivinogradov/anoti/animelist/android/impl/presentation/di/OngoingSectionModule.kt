package com.alekseivinogradov.anoti.animelist.android.impl.presentation.di

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.ongoingsection.OngoingSectionExecutorFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.ongoingsection.OngoingSectionExecutorImpl
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.ongoingsection.OngoingSectionStoreFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchOngoingAnimeListUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper.OngoingUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
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
        fun provideOngoingSectionExecutorFactory(
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
