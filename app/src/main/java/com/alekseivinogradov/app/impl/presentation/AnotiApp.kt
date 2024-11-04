package com.alekseivinogradov.app.impl.presentation

import android.app.Application
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.alekseivinogradov.anime_background_update.api.domain.source.AnimeBackgroundUpdateSource
import com.alekseivinogradov.anime_background_update.impl.data.source.AnimeBackgroundUpdateSourceImpl
import com.alekseivinogradov.anime_background_update.impl.domain.usecase.FetchAnimeListByIdsUsecase
import com.alekseivinogradov.anime_background_update.impl.domain.worker.AnimeUpdateWorker
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiServicePlatform
import com.alekseivinogradov.anime_base.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.celebrity.api.domain.DEFAULT_ANIME_UPDATE_WORK_INTERVAL
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.impl.domain.coroutine_context.CoroutineContextProviderPlatform
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.room.impl.data.AnimeDatabase
import com.alekseivinogradov.database.room.impl.data.repository.AnimeDatabaseRepositoryImpl
import com.alekseivinogradov.network.impl.data.SafeApiImpl
import java.util.concurrent.TimeUnit

class AnotiApp : Application() {

    private val shikimoriService: ShikimoriApiService = ShikimoriApiServiceImpl(
        servicePlatform = ShikimoriApiServicePlatform.instance
    )

    private val animeBackgroundUpdateSource: AnimeBackgroundUpdateSource =
        AnimeBackgroundUpdateSourceImpl(
            service = shikimoriService,
            safeApi = SafeApiImpl
        )

    private val fetchAnimeListByIdsUsecase = FetchAnimeListByIdsUsecase(
        source = animeBackgroundUpdateSource
    )

    private val coroutineContextProvider: CoroutineContextProvider
            by lazy(LazyThreadSafetyMode.NONE) {
                CoroutineContextProviderPlatform(appContext = this)
            }


    private val animeDatabase: AnimeDatabase
            by lazy(LazyThreadSafetyMode.NONE) { AnimeDatabase.getDatabase(appContext = this) }

    private val animeDatabaseRepository: AnimeDatabaseRepository
            by lazy(LazyThreadSafetyMode.NONE) {
                AnimeDatabaseRepositoryImpl(animeDao = animeDatabase.animeDao())
            }

    private val workerFactory by lazy(LazyThreadSafetyMode.NONE) {
        AnimeUpdateWorker.Factory(
            coroutineContextProvider = coroutineContextProvider,
            animeDatabaseRepository = animeDatabaseRepository,
            fetchAnimeListByIdsUsecase = fetchAnimeListByIdsUsecase
        )
    }

    private val workManagerConfig: Configuration by lazy(LazyThreadSafetyMode.NONE) {
        Configuration.Builder().setWorkerFactory(workerFactory).build()
    }

    private val animeUpdatePeriodicWork: PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<AnimeUpdateWorker>(
            repeatInterval = DEFAULT_ANIME_UPDATE_WORK_INTERVAL,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        ).build()

    override fun onCreate() {
        super.onCreate()
        setupWorkManager()
    }

    private fun setupWorkManager() {
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
}
