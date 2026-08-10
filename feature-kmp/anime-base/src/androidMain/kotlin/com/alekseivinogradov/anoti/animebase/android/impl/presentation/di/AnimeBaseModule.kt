package com.alekseivinogradov.anoti.animebase.android.impl.presentation.di

import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animebase.kmp.impl.data.service.ShikimoriApiServiceImpl
import dagger.Module
import dagger.Provides
import io.ktor.client.HttpClient
import javax.inject.Singleton

/**
 * Dagger module providing anime base dependencies for Android.
 */
@Module
interface AnimeBaseModule {
    companion object {
        /**
         * Provides the Shikimori API service implementation.
         *
         * @param httpClient the HTTP client for making API requests.
         */
        @Provides
        @Singleton
        fun provideShikimoriApiService(
            httpClient: HttpClient
        ): ShikimoriApiService = ShikimoriApiServiceImpl(httpClient)
    }
}
