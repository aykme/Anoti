package com.alekseivinogradov.anoti.network.ios.impl.di

import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.alekseivinogradov.anoti.network.kmp.impl.data.client.createHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import me.tatarka.inject.annotations.Provides

/**
 * Contributes the iOS [HttpClient] binding (Darwin engine) to [AppScope]'s merged component.
 */
interface DiNetworkPlatformComponent {
    @Provides
    @AppScope
    fun provideHttpClient(): HttpClient = createHttpClient(Darwin.create())
}
