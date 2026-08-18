package com.alekseivinogradov.anoti.network.ios.impl.di

import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.alekseivinogradov.anoti.network.kmp.impl.data.client.createHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import me.tatarka.inject.annotations.Provides

/**
 * Provides the iOS [HttpClient] binding (Darwin engine); mixed into `core-kmp:di`'s
 * `DiAppComponent`.
 */
interface DiNetworkPlatformComponent {
    @Provides
    @AppScope
    fun provideHttpClient(): HttpClient = createHttpClient(Darwin.create())
}
