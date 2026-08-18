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
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.AnimeUpdateWorker
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
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn
import java.util.concurrent.TimeUnit

/**
 * Contributes the Android [AnimeUpdateManager], the app's [WorkManager] handle and its work
 * requests, and the WorkManager-backed [AnimeBackgroundScheduler]/
 * [UpdateAllAnimeInBackgroundOnceUsecase] bindings to [AppScope]'s merged component.
 */
@ContributesTo(AppScope::class)
interface DiAnimeBackgroundUpdatePlatformComponent {
    @Provides
    @SingleIn(AppScope::class)
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
    @AnimeBackgroundUpdate
    fun provideWorkManagerConfig(workerFactory: AnimeUpdateWorker.Factory): Configuration =
        Configuration.Builder().setWorkerFactory(workerFactory).build()

    /**
     * The app's single [WorkManager] handle.
     *
     * The app manifest removes WorkManager's default `androidx.startup` initializer, so this
     * binding is also the one place that installs the custom [Configuration] carrying the anime
     * update worker factory. Everything that needs WorkManager depends on *this* binding rather
     * than calling `WorkManager.getInstance` itself, so no consumer can observe an uninitialized
     * WorkManager no matter which graph accessor is read first.
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideWorkManager(
        @AppContext appContext: PlatformContext,
        @AnimeBackgroundUpdate workManagerConfiguration: Configuration
    ): WorkManager {
        WorkManager.initialize(context = appContext, configuration = workManagerConfiguration)
        return WorkManager.getInstance(context = appContext)
    }

    @Provides
    fun provideUpdateAllAnimeInBackgroundOnceUsecase(
        workManager: WorkManager,
        @AnimeBackgroundUpdate animeUpdateOnceWork: OneTimeWorkRequest
    ): UpdateAllAnimeInBackgroundOnceUsecase = UpdateAllAnimeInBackgroundOnceUsecaseImpl(
        workManager = workManager,
        updateWork = animeUpdateOnceWork,
        uniqueWorkName = ANIME_UPDATE_ONCE_WORK_NAME
    )

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
        workManager: WorkManager,
        @AnimeBackgroundUpdate animeUpdatePeriodicWork: PeriodicWorkRequest
    ): AnimeBackgroundScheduler = AnimeBackgroundSchedulerImpl(
        workManager = workManager,
        animeUpdatePeriodicWork = animeUpdatePeriodicWork
    )
}
