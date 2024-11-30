package com.alekseivinogradov.anime_background_update.impl.presentation.di

import com.alekseivinogradov.anime_background_update.api.domain.source.AnimeBackgroundUpdateSource
import com.alekseivinogradov.anime_background_update.impl.data.source.AnimeBackgroundUpdateSourceImpl
import com.alekseivinogradov.anime_background_update.impl.domain.usecase.FetchAnimeListByIdsUsecase
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_base.impl.presentation.di.AnimeBaseModule
import com.alekseivinogradov.anime_database.room.impl.presentation.di.anime_background_update.AnimeDatabaseBackgroundUpdateModule
import com.alekseivinogradov.anime_database.room.impl.presentation.di.base.AnimeDatabaseBaseModule
import com.alekseivinogradov.network.api.data.SafeApi
import com.alekseivinogradov.network.impl.presentation.di.NetworkModule
import dagger.Module
import dagger.Provides

@Module(
    includes = [
        AnimeBaseModule::class,
        AnimeDatabaseBaseModule::class,
        AnimeDatabaseBackgroundUpdateModule::class,
        NetworkModule::class
    ]
)
interface AnimeBackgroundUpdateModule {
    companion object {
        @Provides
        fun provideAnimeBackgroundUpdateSource(
            service: ShikimoriApiService,
            safeApi: SafeApi
        ): AnimeBackgroundUpdateSource = AnimeBackgroundUpdateSourceImpl(
            service = service,
            safeApi = safeApi
        )

        @Provides
        fun provideFetchAnimeListByIdsUsecase(
            source: AnimeBackgroundUpdateSource
        ): FetchAnimeListByIdsUsecase = FetchAnimeListByIdsUsecase(source)
    }
}
