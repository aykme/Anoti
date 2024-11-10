package com.alekseivinogradov.app.impl.presentation

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.alekseivinogradov.anime_background_update.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anime_background_update.api.domain.source.AnimeBackgroundUpdateSource
import com.alekseivinogradov.anime_background_update.impl.data.source.AnimeBackgroundUpdateSourceImpl
import com.alekseivinogradov.anime_background_update.impl.domain.manager.AnimeUpdateManagerImpl
import com.alekseivinogradov.anime_background_update.impl.domain.usecase.FetchAnimeListByIdsUsecase
import com.alekseivinogradov.anime_background_update.impl.domain.worker.AnimeUpdateWorker
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiServicePlatform
import com.alekseivinogradov.anime_base.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.anime_notification.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anime_notification.impl.presentation.factory.AnimeNotificationChannelFactory
import com.alekseivinogradov.anime_notification.impl.presentation.manager.AnimeNotificationManagerImpl
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.impl.domain.coroutine_context.CoroutineContextProviderPlatform
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.api.domain.usecase.FetchAllDatabaseItemsUsecase
import com.alekseivinogradov.database.api.domain.usecase.UpdateDatabaseItemUsecase
import com.alekseivinogradov.database.impl.domain.usecase.FetchAllDatabaseItemsUsecaseImpl
import com.alekseivinogradov.database.impl.domain.usecase.UpdateDatabaseItemUsecaseImpl
import com.alekseivinogradov.database.room.impl.data.AnimeDatabase
import com.alekseivinogradov.database.room.impl.data.repository.AnimeDatabaseRepositoryImpl
import com.alekseivinogradov.network.api.data.SafeApi
import com.alekseivinogradov.network.impl.data.SafeApiImpl
import java.util.concurrent.TimeUnit

class AnotiApp : Application() {

    private val coroutineContextProvider: CoroutineContextProvider by lazy {
        CoroutineContextProviderPlatform(appContext = this)
    }

    private val shikimoriService: ShikimoriApiService = ShikimoriApiServiceImpl(
        servicePlatform = ShikimoriApiServicePlatform.instance
    )

    private val safeApp: SafeApi = SafeApiImpl

    private val animeDatabase: AnimeDatabase
            by lazy(LazyThreadSafetyMode.NONE) { AnimeDatabase.getDatabase(appContext = this) }

    private val animeDatabaseRepository: AnimeDatabaseRepository
            by lazy(LazyThreadSafetyMode.NONE) {
                AnimeDatabaseRepositoryImpl(animeDao = animeDatabase.animeDao())
            }

    private val updateDatabaseItemUsecase: UpdateDatabaseItemUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                UpdateDatabaseItemUsecaseImpl(repository = animeDatabaseRepository)
            }

    private val fetchAllDatabaseItemsUsecase: FetchAllDatabaseItemsUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                FetchAllDatabaseItemsUsecaseImpl(repository = animeDatabaseRepository)
            }

    private val animeBackgroundUpdateSource: AnimeBackgroundUpdateSource =
        AnimeBackgroundUpdateSourceImpl(
            service = shikimoriService,
            safeApi = safeApp
        )

    private val fetchAnimeListByIdsUsecase = FetchAnimeListByIdsUsecase(
        source = animeBackgroundUpdateSource
    )

    private val animeNotificationManager: AnimeNotificationManager
            by lazy(LazyThreadSafetyMode.NONE) {
                AnimeNotificationManagerImpl(appContext = applicationContext)
            }

    private val animeUpdateManager: AnimeUpdateManager by lazy(LazyThreadSafetyMode.NONE) {
        AnimeUpdateManagerImpl(
            coroutineContextProvider = coroutineContextProvider,
            fetchAllDatabaseItemsUsecase = fetchAllDatabaseItemsUsecase,
            fetchAnimeListByIdsUsecase = fetchAnimeListByIdsUsecase,
            updateDatabaseItemUsecase = updateDatabaseItemUsecase,
            notificationManager = animeNotificationManager
        )
    }

    private val workerFactory by lazy(LazyThreadSafetyMode.NONE) {
        AnimeUpdateWorker.Factory(
            animeUpdateManager = animeUpdateManager
        )
    }

    private val workManagerConfig: Configuration by lazy(LazyThreadSafetyMode.NONE) {
        Configuration.Builder().setWorkerFactory(workerFactory).build()
    }

    private val animeUpdatePeriodicWork: PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<AnimeUpdateWorker>(
            repeatInterval = AnimeUpdateManager.defaultAnimeUpdateWorkIntervalMinutes,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        ).build()

    private val animeNotificationChannelFactory
            by lazy(LazyThreadSafetyMode.NONE) {
                AnimeNotificationChannelFactory()
            }

    override fun onCreate() {
        super.onCreate()
        setupAnimeUpdateWorkManager()
        setupAnimeNotificationManager()
    }

    private fun setupAnimeUpdateWorkManager() {
        WorkManager.initialize(
            /* context = */ this,
            /* configuration = */ workManagerConfig
        )
        WorkManager.getInstance(/* context = */ this)
            .enqueueUniquePeriodicWork(
                /* uniqueWorkName = */ AnimeUpdateWorker.animeUpdatePeriodicWorkName,
                /* existingPeriodicWorkPolicy = */ ExistingPeriodicWorkPolicy.KEEP,
                /* periodicWork = */ animeUpdatePeriodicWork
            )
    }

    private fun setupAnimeNotificationManager() {
        (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)
            ?.let { notificationManager: NotificationManager ->
                notificationManager.createNotificationChannel(
                    animeNotificationChannelFactory.create(applicationContext)
                )
            }
    }
}
