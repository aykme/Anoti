package com.alekseivinogradov.anoti.network.android.impl.di

import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.alekseivinogradov.anoti.network.kmp.impl.data.client.createHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import me.tatarka.inject.annotations.Provides

/**
 * Provides the Android [HttpClient] binding (OkHttp engine); mixed into `:app`'s
 * `DiAppComponent`.
 */
interface DiNetworkPlatformComponent {
    @Provides
    @AppScope
    fun provideHttpClient(): HttpClient = createHttpClient(OkHttp.create())
}
