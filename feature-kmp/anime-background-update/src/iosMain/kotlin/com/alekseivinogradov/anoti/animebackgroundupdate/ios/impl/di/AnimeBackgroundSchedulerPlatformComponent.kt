package com.alekseivinogradov.anoti.animebackgroundupdate.ios.impl.di

import com.alekseivinogradov.anoti.animebackgroundupdate.ios.impl.domain.scheduler.AnimeBackgroundSchedulerImpl
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.scheduler.AnimeBackgroundScheduler
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * Contributes the iOS [AnimeBackgroundScheduler] binding to [AppScope]'s merged component,
 * registering the `BGAppRefreshTask` handler as soon as the scheduler is created. See
 * [AnimeBackgroundSchedulerImpl]'s KDoc for the Info.plist registration gap this depends on.
 */
@ContributesTo(AppScope::class)
interface AnimeBackgroundSchedulerPlatformComponent {
    @Provides
    @SingleIn(AppScope::class)
    fun provideAnimeBackgroundScheduler(
        animeUpdateManager: AnimeUpdateManager
    ): AnimeBackgroundScheduler = AnimeBackgroundSchedulerImpl(
        animeUpdateManager = animeUpdateManager,
        coroutineScope = CoroutineScope(SupervisorJob())
    ).also { it.registerTaskHandler() }
}
