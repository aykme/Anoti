package com.alekseivinogradov.anoti.animelist.android.impl.presentation.di

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.announcedsection.AnnouncedSectionExecutorFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.announcedsection.AnnouncedSectionExecutorImpl
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.announcedsection.AnnouncedSectionStoreFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnnouncedAnimeListUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper.AnnouncedUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dagger.Module
import dagger.Provides

@Module
interface AnnouncedSectionModule {
    companion object {
        @Provides
        fun provideFetchAnnouncedAnimeListUsecase(
            source: AnimeListSource
        ): FetchAnnouncedAnimeListUsecase = FetchAnnouncedAnimeListUsecase(source)

        @Provides
        fun provideAnnouncedUsecases(
            fetchAnnouncedAnimeListUsecase: FetchAnnouncedAnimeListUsecase
        ): AnnouncedUsecases {
            return AnnouncedUsecases(
                fetchAnnouncedAnimeListUsecase = fetchAnnouncedAnimeListUsecase
            )
        }

        @Provides
        fun provideAnnouncedSectionExecutorFactory(
            coroutineContextProvider: CoroutineContextProvider,
            usecases: AnnouncedUsecases,
            toastProvider: ToastProvider
        ): AnnouncedSectionExecutorFactory = {
            AnnouncedSectionExecutorImpl(
                coroutineContextProvider = coroutineContextProvider,
                usecases = usecases,
                toastProvider = toastProvider
            )
        }

        @Provides
        fun provideAnnouncedSectionStore(
            storeFactory: StoreFactory,
            executorFactory: AnnouncedSectionExecutorFactory
        ): AnnouncedSectionStore {
            return AnnouncedSectionStoreFactory(
                storeFactory = storeFactory,
                executorFactory = executorFactory
            ).create()
        }
    }
}
