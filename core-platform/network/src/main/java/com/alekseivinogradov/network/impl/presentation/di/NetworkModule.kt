package com.alekseivinogradov.network.impl.presentation.di

import com.alekseivinogradov.network.api.data.SafeApi
import com.alekseivinogradov.network.impl.data.SafeApiImpl
import dagger.Module
import dagger.Provides
import kotlin.time.Duration.Companion.milliseconds

@Module
interface NetworkModule {
    companion object {
        @Provides
        fun provideSafeApi(): SafeApi = SafeApiImpl(
            maxAttempt = 3,
            attemptDelay = 2500.milliseconds
        )
    }
}
