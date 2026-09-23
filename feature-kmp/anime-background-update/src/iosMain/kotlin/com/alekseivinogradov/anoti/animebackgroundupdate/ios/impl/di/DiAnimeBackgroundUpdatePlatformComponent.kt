package com.alekseivinogradov.anoti.animebackgroundupdate.ios.impl.di

import com.alekseivinogradov.anoti.animebackgroundupdate.ios.impl.domain.scheduler.AnimeBackgroundSchedulerImpl
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.scheduler.AnimeBackgroundScheduler
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.manager.AnimeUpdateManagerImpl
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.usecase.FetchAnimeListByIdsUsecase
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.usecase.SingleFlightUpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.FetchAllAnimeDatabaseItemsUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.UpdateAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import me.tatarka.inject.annotations.Provides

/**
 * Provides the iOS [AnimeUpdateManager], [AnimeBackgroundScheduler] and
 * [UpdateAllAnimeInBackgroundOnceUsecase] bindings; mixed into `core-kmp:di-app`'s
 * `DiAppComponent` — the mirror of `androidMain`'s `DiAnimeBackgroundUpdatePlatformComponent`,
 * minus the WorkManager plumbing iOS has no use for.
 */
interface DiAnimeBackgroundUpdatePlatformComponent {
    @Provides
    @AppScope
    fun provideAnimeUpdateManager(
        coroutineContextProvider: CoroutineContextProvider,
        fetchAllAnimeDatabaseItemsUsecase: FetchAllAnimeDatabaseItemsUsecase,
        fetchAnimeListByIdsUsecase: FetchAnimeListByIdsUsecase,
        updateAnimeDatabaseItemUsecase: UpdateAnimeDatabaseItemUsecase,
        notificationManager: AnimeNotificationManager
    ): AnimeUpdateManager = AnimeUpdateManagerImpl(
        coroutineContextProvider = coroutineContextProvider,
        fetchAllAnimeDatabaseItemsUsecase = fetchAllAnimeDatabaseItemsUsecase,
        fetchAnimeListByIdsUsecase = fetchAnimeListByIdsUsecase,
        updateAnimeDatabaseItemUsecase = updateAnimeDatabaseItemUsecase,
        notificationManager = notificationManager
    )

    /**
     * Registers the `BGAppRefreshTask` handler as soon as the scheduler is created. The task
     * identifier must also be listed in the iOS app target's Info.plist under
     * `BGTaskSchedulerPermittedIdentifiers` — see [AnimeBackgroundSchedulerImpl]'s KDoc.
     */
    @Provides
    @AppScope
    fun provideAnimeBackgroundScheduler(
        animeUpdateManager: AnimeUpdateManager
    ): AnimeBackgroundScheduler = AnimeBackgroundSchedulerImpl(
        animeUpdateManager = animeUpdateManager,
        coroutineScope = updatePassScope()
    ).also { it.registerTaskHandler() }

    /**
     * `@AppScope` is load-bearing: the single-flight guard of
     * [SingleFlightUpdateAllAnimeInBackgroundOnceUsecase] lives in the instance, so every
     * injection point must share one. Without the scope, each would get its own guard and
     * concurrent passes could run.
     */
    @Provides
    @AppScope
    fun provideUpdateAllAnimeInBackgroundOnceUsecase(
        animeUpdateManager: AnimeUpdateManager
    ): UpdateAllAnimeInBackgroundOnceUsecase = SingleFlightUpdateAllAnimeInBackgroundOnceUsecase(
        animeUpdateManager = animeUpdateManager,
        coroutineScope = updatePassScope()
    )
}

private const val TAG = "DiAnimeBackgroundUpdatePlatformComponent"

/**
 * A scope for update passes. A throw out of one is reported rather than left to end the
 * process, which is what an unhandled one does on this platform.
 */
private fun updatePassScope(): CoroutineScope = CoroutineScope(
    SupervisorJob() + CoroutineExceptionHandler { _, throwable: Throwable ->
        println("$TAG: an update pass ended in $throwable")
    }
)
