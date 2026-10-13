package com.alekseivinogradov.anoti.animebase.kmp.impl.di

import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.source.AnimePosterSource
import com.alekseivinogradov.anoti.animebase.kmp.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.anoti.animebase.kmp.impl.data.source.AnimePosterSourceImpl
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import io.ktor.client.HttpClient
import me.tatarka.inject.annotations.Provides

/**
 * Provides the [ShikimoriApiService] and [AnimePosterSource] bindings; mixed into
 * `DiAppComponent` on both platforms.
 */
interface DiAnimeBaseComponent {
    @Provides
    @AppScope
    fun provideShikimoriApiService(httpClient: HttpClient): ShikimoriApiService =
        ShikimoriApiServiceImpl(httpClient)

    /** The poster lookup; stateless, so every reader gets its own. */
    @Provides
    fun provideAnimePosterSource(
        service: ShikimoriApiService,
        safeApi: SafeApi
    ): AnimePosterSource = AnimePosterSourceImpl(service = service, safeApi = safeApi)
}
