package com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.presentation.di

import androidx.work.Configuration
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.scheduler.AnimeBackgroundSchedulerImpl
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecaseImpl
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.ANIME_UPDATE_ONCE_WORK_NAME
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.ANIME_UPDATE_WORK_CONSTRAINTS
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.AnimeUpdateWorker
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.scheduler.AnimeBackgroundScheduler
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.di.kmp.PlatformContext
import com.alekseivinogradov.anoti.di.kmp.qualifier.AnimeBackgroundUpdate
import com.alekseivinogradov.anoti.di.kmp.qualifier.AppContext
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import me.tatarka.inject.annotations.Provides
import java.util.concurrent.TimeUnit

/**
 * Provides the app's [WorkManager] handle and its work requests, and the WorkManager-backed
 * [AnimeBackgroundScheduler]/[UpdateAllAnimeInBackgroundOnceUsecase] bindings; mixed into
 * `core-kmp:di-app`'s Android `DiAppComponent`. The [AnimeUpdateManager] the worker runs comes
 * from the common `DiAnimeBackgroundUpdateComponent`.
 */
interface DiAnimeBackgroundUpdatePlatformComponent {
    @Provides
    @AnimeBackgroundUpdate
    fun provideAnimeUpdateOnceWork(): OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<AnimeUpdateWorker>()
            .setConstraints(ANIME_UPDATE_WORK_CONSTRAINTS)
            .build()

    @Provides
    @AnimeBackgroundUpdate
    fun provideWorkManagerConfig(workerFactory: AnimeUpdateWorker.Factory): Configuration =
        Configuration.Builder().setWorkerFactory(workerFactory).build()

    /**
     * WorkManager is reached through a function rather than bound as a dependency. Reaching
     * for it here would lock it while this graph is locked, and WorkManager asks this same
     * graph for its configuration, so the two would wait on each other.
     */
    @Provides
    fun provideUpdateAllAnimeInBackgroundOnceUsecase(
        @AppContext appContext: PlatformContext,
        @AnimeBackgroundUpdate animeUpdateOnceWork: OneTimeWorkRequest
    ): UpdateAllAnimeInBackgroundOnceUsecase = UpdateAllAnimeInBackgroundOnceUsecaseImpl(
        workManager = { WorkManager.getInstance(context = appContext) },
        updateWork = animeUpdateOnceWork,
        uniqueWorkName = ANIME_UPDATE_ONCE_WORK_NAME
    )

    @Provides
    @AnimeBackgroundUpdate
    fun provideAnimeUpdatePeriodicWork(): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<AnimeUpdateWorker>(
            repeatInterval = AnimeUpdateManager.DEFAULT_ANIME_UPDATE_INTERVAL_MINUTES,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(ANIME_UPDATE_WORK_CONSTRAINTS)
            .build()

    @Provides
    @AppScope
    fun provideAnimeBackgroundScheduler(
        @AppContext appContext: PlatformContext,
        @AnimeBackgroundUpdate animeUpdatePeriodicWork: PeriodicWorkRequest
    ): AnimeBackgroundScheduler = AnimeBackgroundSchedulerImpl(
        workManager = { WorkManager.getInstance(context = appContext) },
        animeUpdatePeriodicWork = animeUpdatePeriodicWork
    )
}
