package com.alekseivinogradov.anime_base.impl.presentation.di

import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiServicePlatform
import com.alekseivinogradov.anime_base.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.network.api.domain.SHIKIMORI_BASE_URL
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
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
        fun provideShikimoriRetrofit(
            retrofitBuilder: Retrofit.Builder
        ): Retrofit = retrofitBuilder
            .baseUrl(SHIKIMORI_BASE_URL)
            .build()

        @Provides
        @Singleton
        fun provideShikimoriServicePlatform(
            retrofit: Retrofit
        ): ShikimoriApiServicePlatform =
            retrofit.create(ShikimoriApiServicePlatform::class.java)
    }
}
