package com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.presentation.di

import android.content.Context
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.scheduler.AnimeBackgroundSchedulerImpl
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecaseImpl
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.AnimeUpdateWorker
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.animeUpdateOnceWorkName
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.scheduler.AnimeBackgroundScheduler
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.manager.AnimeUpdateManagerImpl
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.usecase.FetchAnimeListByIdsUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.FetchAllAnimeDatabaseItemsUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.UpdateAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.di.kmp.PlatformContext
import com.alekseivinogradov.anoti.di.kmp.qualifier.AnimeBackgroundUpdate
import com.alekseivinogradov.anoti.di.kmp.qualifier.AppContext
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import java.util.concurrent.TimeUnit
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * Contributes the Android [AnimeUpdateManager], its WorkManager requests and the WorkManager-backed
 * [AnimeBackgroundScheduler]/[UpdateAllAnimeInBackgroundOnceUsecase] bindings to [AppScope]'s
 * merged component.
 */
@ContributesTo(AppScope::class)
interface AnimeBackgroundUpdatePlatformComponent {
    @Provides
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

    @Provides
    @AnimeBackgroundUpdate
    fun provideAnimeUpdateOnceWork(): OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<AnimeUpdateWorker>().build()

    @Provides
    fun provideUpdateAllAnimeInBackgroundOnceUsecase(
        @AppContext appContext: PlatformContext,
        @AnimeBackgroundUpdate animeUpdateOnceWork: OneTimeWorkRequest
    ): UpdateAllAnimeInBackgroundOnceUsecase = UpdateAllAnimeInBackgroundOnceUsecaseImpl(
        workManager = WorkManager.getInstance(appContext as Context),
        updateWork = animeUpdateOnceWork,
        uniqueWorkName = animeUpdateOnceWorkName
    )

    @Provides
    @AnimeBackgroundUpdate
    fun provideWorkManagerConfig(workerFactory: AnimeUpdateWorker.Factory): Configuration =
        Configuration.Builder().setWorkerFactory(workerFactory).build()

    @Provides
    @AnimeBackgroundUpdate
    fun provideAnimeUpdatePeriodicWork(): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<AnimeUpdateWorker>(
            repeatInterval = AnimeUpdateManager.DEFAULT_ANIME_UPDATE_INTERVAL_MINUTES,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        ).build()

    @Provides
    @SingleIn(AppScope::class)
    fun provideAnimeBackgroundScheduler(
        @AppContext appContext: PlatformContext,
        @AnimeBackgroundUpdate workManagerConfiguration: Configuration,
        @AnimeBackgroundUpdate animeUpdatePeriodicWork: PeriodicWorkRequest
    ): AnimeBackgroundScheduler = AnimeBackgroundSchedulerImpl(
        appContext = appContext as Context,
        workManagerConfiguration = workManagerConfiguration,
        animeUpdatePeriodicWork = animeUpdatePeriodicWork
    ).also { it.initializeWorkManager() }
}
