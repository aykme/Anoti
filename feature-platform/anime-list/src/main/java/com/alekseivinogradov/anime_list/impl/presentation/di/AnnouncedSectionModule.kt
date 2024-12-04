package com.alekseivinogradov.anime_list.impl.presentation.di

import com.alekseivinogradov.anime_list.api.domain.source.AnimeListSource
import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.alekseivinogradov.anime_list.impl.domain.store.announced_section.AnnouncedSectionExecutorFactory
import com.alekseivinogradov.anime_list.impl.domain.store.announced_section.AnnouncedSectionExecutorImpl
import com.alekseivinogradov.anime_list.impl.domain.store.announced_section.AnnouncedSectionStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnnouncedAnimeListUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.AnnouncedUsecases
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.api.domain.toast.provider.ToastProvider
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
        fun povideAnnouncedSectionExecutorFactory(
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
