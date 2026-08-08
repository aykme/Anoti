package com.alekseivinogradov.anoti.animebackgroundupdate.platform.impl.presentation.di

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.source.AnimeBackgroundUpdateSource
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.data.source.AnimeBackgroundUpdateSourceImpl
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.usecase.FetchAnimeListByIdsUsecase
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
interface AnimeBaseBackgroundUpdateModule {
    companion object {
        @Provides
        @Singleton
        fun provideAnimeBackgroundUpdateSource(
            service: ShikimoriApiService,
            safeApi: SafeApi
        ): AnimeBackgroundUpdateSource = AnimeBackgroundUpdateSourceImpl(
            service = service,
            safeApi = safeApi
        )

        @Provides
        @Singleton
        fun provideFetchAnimeListByIdsUsecase(
            source: AnimeBackgroundUpdateSource
        ): FetchAnimeListByIdsUsecase = FetchAnimeListByIdsUsecase(source)
    }
}
