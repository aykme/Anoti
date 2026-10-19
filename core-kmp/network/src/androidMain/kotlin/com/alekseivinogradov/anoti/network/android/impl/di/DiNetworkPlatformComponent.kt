package com.alekseivinogradov.anoti.network.android.impl.di

import coil3.ImageLoader
import com.alekseivinogradov.anoti.di.kmp.PlatformContext
import com.alekseivinogradov.anoti.di.kmp.qualifier.AppContext
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.alekseivinogradov.anoti.network.kmp.impl.data.client.createHttpClient
import com.alekseivinogradov.anoti.network.kmp.impl.data.client.createImageHttpClient
import com.alekseivinogradov.anoti.network.kmp.impl.data.client.createImageLoader
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import me.tatarka.inject.annotations.Provides

/**
 * Provides the Android [HttpClient] and [ImageLoader] bindings (OkHttp engine); mixed into
 * `:androidApp`'s `DiAppComponent`.
 */
interface DiNetworkPlatformComponent {
    @Provides
    @AppScope
    fun provideHttpClient(): HttpClient = createHttpClient(OkHttp.create())

    /** The app-wide loader, so every screen and notification shares one cache. */
    @Provides
    @AppScope
    fun provideImageLoader(@AppContext appContext: PlatformContext): ImageLoader =
        createImageLoader(context = appContext) { createImageHttpClient(OkHttp.create()) }
}
