package com.alekseivinogradov.anoti.network.kmp.impl.di

import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.alekseivinogradov.anoti.network.kmp.impl.data.SafeApiImpl
import me.tatarka.inject.annotations.Provides
import kotlin.time.Duration.Companion.milliseconds

/**
 * Provides the [SafeApi] binding; mixed into `DiAppComponent` on both platforms.
 */
interface DiNetworkComponent {
    @Provides
    @AppScope
    fun provideSafeApi(): SafeApi = SafeApiImpl(
        maxAttempt = 3,
        attemptDelay = 2500.milliseconds
    )
}
