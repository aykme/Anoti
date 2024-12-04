package com.alekseivinogradov.anime_list.impl.presentation.di

import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_list.api.domain.source.AnimeListSource
import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anime_list.impl.data.source.AnimeListSourceImpl
import com.alekseivinogradov.anime_list.impl.domain.store.main.AnimeListExecutorFactory
import com.alekseivinogradov.anime_list.impl.domain.store.main.AnimeListExecutorImpl
import com.alekseivinogradov.anime_list.impl.domain.store.main.AnimeListMainStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.network.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dagger.Module
import dagger.Provides

@Module(
    includes = [
        OngoingSectionModule::class,
        AnnouncedSectionModule::class,
        SearchSectionModule::class
    ]
)
interface AnimeListModule {
    companion object {
        @Provides
        fun provideAnimeListSource(
            service: ShikimoriApiService,
            safeApi: SafeApi
        ): AnimeListSource {
            return AnimeListSourceImpl(
                service = service,
                safeApi = safeApi
            )
        }

        @Provides
        fun provideFetchAnimeDetailsByIdUsecase(
            source: AnimeListSource
        ): FetchAnimeDetailsByIdUsecase = FetchAnimeDetailsByIdUsecase(source)

        @Provides
        fun provideAnimeListExecutorFactory(
            coroutineContextProvider: CoroutineContextProvider
        ): AnimeListExecutorFactory =
            {
                AnimeListExecutorImpl(
                    coroutineContextProvider = coroutineContextProvider
                )
            }

        @Provides
        fun provideAnimeListMainStore(
            storeFactory: StoreFactory,
            executorFactory: AnimeListExecutorFactory
        ): AnimeListMainStore {
            return AnimeListMainStoreFactory(
                storeFactory = storeFactory,
                executorFactory = executorFactory
            ).create()
        }
    }
}
