package com.alekseivinogradov.app.impl.presentation.di

import com.alekseivinogradov.anime_background_update.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anime_background_update.impl.domain.manager.AnimeUpdateManagerImpl
import com.alekseivinogradov.anime_background_update.impl.domain.usecase.FetchAnimeListByIdsUsecase
import com.alekseivinogradov.anime_background_update.impl.presentation.di.AnimeBaseBackgroundUpdateModule
import com.alekseivinogradov.anime_background_update.impl.presentation.di.AnimePeriodicBackgroundUpdateModule
import com.alekseivinogradov.anime_base.impl.presentation.di.AnimeBaseModule
import com.alekseivinogradov.anime_database.api.domain.usecase.FetchAllAnimeDatabaseItemsUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.UpdateAnimeDatabaseItemUsecase
import com.alekseivinogradov.anime_database.room.impl.presentation.di.AnimeDatabaseCompletedModule
import com.alekseivinogradov.anime_notification.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anime_notification.impl.presentation.di.AnimeNotificationModule
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.impl.presentation.di.CelebrityAppModule
import com.alekseivinogradov.network.impl.presentation.di.NetworkModule
import dagger.Module
import dagger.Provides

@Module(
    includes = [
        CelebrityAppModule::class,
        AnimeDatabaseCompletedModule::class,
        AnimeBaseModule::class,
        NetworkModule::class,
        AnimeBaseBackgroundUpdateModule::class,
        AnimePeriodicBackgroundUpdateModule::class,
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
    }
}
