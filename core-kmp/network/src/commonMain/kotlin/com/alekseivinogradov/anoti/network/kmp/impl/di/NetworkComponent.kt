package com.alekseivinogradov.anoti.network.kmp.impl.di

import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.alekseivinogradov.anoti.network.kmp.impl.data.SafeApiImpl
import kotlin.time.Duration.Companion.milliseconds
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * Contributes the [SafeApi] binding to [AppScope]'s merged component.
 */
@ContributesTo(AppScope::class)
interface NetworkComponent {
    @Provides
    @SingleIn(AppScope::class)
    fun provideSafeApi(): SafeApi = SafeApiImpl(
        maxAttempt = 3,
        attemptDelay = 2500.milliseconds
    )
}
