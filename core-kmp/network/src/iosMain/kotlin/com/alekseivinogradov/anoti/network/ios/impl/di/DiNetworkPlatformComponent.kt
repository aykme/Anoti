package com.alekseivinogradov.anoti.network.ios.impl.di

import coil3.ImageLoader
import coil3.PlatformContext
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.alekseivinogradov.anoti.network.kmp.impl.data.client.createHttpClient
import com.alekseivinogradov.anoti.network.kmp.impl.data.client.createImageHttpClient
import com.alekseivinogradov.anoti.network.kmp.impl.data.client.createImageLoader
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import me.tatarka.inject.annotations.Provides

/**
 * Provides the iOS [HttpClient] and [ImageLoader] bindings (Darwin engine); mixed into
 * `core-kmp:di-app`'s `DiAppComponent`.
 */
interface DiNetworkPlatformComponent {
    @Provides
    @AppScope
    fun provideHttpClient(): HttpClient = createHttpClient(Darwin.create())

    /** The app-wide loader, so every screen and notification shares one cache. */
    @Provides
    @AppScope
    fun provideImageLoader(): ImageLoader =
        createImageLoader(context = PlatformContext.INSTANCE) {
            createImageHttpClient(Darwin.create())
        }
}
