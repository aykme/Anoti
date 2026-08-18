package com.alekseivinogradov.anoti.animebase.kmp.impl.di

import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animebase.kmp.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import io.ktor.client.HttpClient
import me.tatarka.inject.annotations.Provides

/** Contributes the [ShikimoriApiService] binding to [AppScope]'s merged component. */
interface DiAnimeBaseComponent {
    @Provides
    @AppScope
    fun provideShikimoriApiService(httpClient: HttpClient): ShikimoriApiService =
        ShikimoriApiServiceImpl(httpClient)
}
