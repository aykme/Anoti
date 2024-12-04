package com.alekseivinogradov.anime_base.impl.presentation.di

import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiServicePlatform
import com.alekseivinogradov.anime_base.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.network.impl.data.shikimoriRetrofit
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
interface AnimeBaseModule {
    companion object {
        @Provides
        @Singleton
        fun provideShikimoriApiService(
            servicePlatform: ShikimoriApiServicePlatform
        ): ShikimoriApiService = ShikimoriApiServiceImpl(servicePlatform)

        @Provides
        @Singleton
        fun provideShikimoriServicePlatform(): ShikimoriApiServicePlatform =
            shikimoriRetrofit.create(ShikimoriApiServicePlatform::class.java)
    }
}
