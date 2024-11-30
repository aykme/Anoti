package com.alekseivinogradov.app.impl.presentation.di

import androidx.work.Configuration
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import com.alekseivinogradov.anime_background_update.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anime_background_update.impl.domain.manager.AnimeUpdateManagerImpl
import com.alekseivinogradov.anime_background_update.impl.domain.usecase.FetchAnimeListByIdsUsecase
import com.alekseivinogradov.anime_background_update.impl.domain.worker.AnimeUpdateWorker
import com.alekseivinogradov.anime_background_update.impl.presentation.di.AnimeBackgroundUpdateModule
import com.alekseivinogradov.anime_notification.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anime_notification.impl.presentation.di.AnimeNotificationModule
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.impl.domain.coroutine_context.CelebrityModule
import com.alekseivinogradov.anime_database.api.domain.usecase.FetchAllAnimeDatabaseItemsUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.UpdateAnimeDatabaseItemUsecase
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import dagger.Module
import dagger.Provides
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module(
    includes = [
        CelebrityModule::class,
        AnimeBackgroundUpdateModule::class,
        AnimeNotificationModule::class
    ]
)
internal interface AppModule {
    companion object {
        @Provides
        fun prvideAnimeUpdateManager(
            coroutineContextProvider: CoroutineContextProvider,
            fetchAllAnimeDatabaseItemsUsecase: FetchAllAnimeDatabaseItemsUsecase,
            fetchAnimeListByIdsUsecase: FetchAnimeListByIdsUsecase,
            updateAnimeDatabaseItemUsecase: UpdateAnimeDatabaseItemUsecase,
            notificationManager: AnimeNotificationManager
        ): AnimeUpdateManager {
            return AnimeUpdateManagerImpl(
                coroutineContextProvider = coroutineContextProvider,
                fetchAllAnimeDatabaseItemsUsecase = fetchAllAnimeDatabaseItemsUsecase,
                fetchAnimeListByIdsUsecase = fetchAnimeListByIdsUsecase,
                updateAnimeDatabaseItemUsecase = updateAnimeDatabaseItemUsecase,
                notificationManager = notificationManager
            )
        }

        @Provides
        fun provideWorkManagerConfig(workerFactory: AnimeUpdateWorker.Factory): Configuration =
            Configuration.Builder().setWorkerFactory(workerFactory).build()

        @Provides
        fun provideAnimeUpdatePeriodicWork(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<AnimeUpdateWorker>(
                repeatInterval = AnimeUpdateManager.DEFAULT_ANIME_UPDATE_INTERVAL_MINUTES,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            ).build()
        }

        @Provides
        @Singleton
        fun provideStoreFactory(): StoreFactory = DefaultStoreFactory()
    }
}
