package com.alekseivinogradov.anoti.animelist.platform.impl.presentation.di

import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.data.source.AnimeListSourceImpl
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.main.AnimeListExecutorFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.main.AnimeListExecutorImpl
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.main.AnimeListMainStoreFactory
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
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
