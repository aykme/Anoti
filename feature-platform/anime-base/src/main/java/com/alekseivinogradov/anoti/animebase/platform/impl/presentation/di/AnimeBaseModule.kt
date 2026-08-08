package com.alekseivinogradov.anoti.animebase.platform.impl.presentation.di

import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animebase.platform.api.data.service.ShikimoriApiServicePlatform
import com.alekseivinogradov.anoti.animebase.platform.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.anoti.network.kmp.api.domain.SHIKIMORI_BASE_URL
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import retrofit2.Retrofit

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
