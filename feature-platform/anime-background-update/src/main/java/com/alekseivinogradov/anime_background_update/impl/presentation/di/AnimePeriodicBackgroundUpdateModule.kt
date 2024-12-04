package com.alekseivinogradov.anime_background_update.impl.presentation.di

import androidx.work.Configuration
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import com.alekseivinogradov.anime_background_update.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anime_background_update.impl.domain.worker.AnimeUpdateWorker
import com.alekseivinogradov.di.api.presentation.AnimeBackgroundUpdate
import dagger.Module
import dagger.Provides
import java.util.concurrent.TimeUnit

@Module
interface AnimePeriodicBackgroundUpdateModule {
    companion object {
        @Provides
        @AnimeBackgroundUpdate
        fun provideWorkManagerConfig(workerFactory: AnimeUpdateWorker.Factory): Configuration =
            Configuration.Builder().setWorkerFactory(workerFactory).build()

        @Provides
        @AnimeBackgroundUpdate
        fun provideAnimeUpdatePeriodicWork(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<AnimeUpdateWorker>(
                repeatInterval = AnimeUpdateManager.DEFAULT_ANIME_UPDATE_INTERVAL_MINUTES,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            ).build()
        }
    }
}
