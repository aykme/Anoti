package com.alekseivinogradov.anoti.network.android.impl.presentation.di

import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.alekseivinogradov.anoti.network.kmp.impl.data.SafeApiImpl
import com.alekseivinogradov.anoti.network.kmp.impl.data.client.createHttpClient
import dagger.Module
import dagger.Provides
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Module
interface NetworkModule {
    companion object {
        @Provides
        @Singleton
        fun provideHttpClient(): HttpClient = createHttpClient(OkHttp.create())

        @Provides
        @Singleton
        fun provideSafeApi(): SafeApi = SafeApiImpl(
            maxAttempt = 3,
            attemptDelay = 2500.milliseconds
        )
    }
}
