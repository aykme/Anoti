package com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.presentation.di

import androidx.work.Configuration
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.AnimeUpdateWorker
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anoti.celebrity.android.api.presentation.di.AnimeBackgroundUpdate
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
