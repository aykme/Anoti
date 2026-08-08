package com.alekseivinogradov.anoti.animebase.platform.impl.presentation.di

import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animebase.kmp.impl.data.service.ShikimoriApiServiceImpl
import dagger.Module
import dagger.Provides
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
interface AnimeBaseModule {
    companion object {
        @Provides
        @Singleton
        fun provideShikimoriApiService(
            httpClient: HttpClient
        ): ShikimoriApiService = ShikimoriApiServiceImpl(httpClient)
    }
}
