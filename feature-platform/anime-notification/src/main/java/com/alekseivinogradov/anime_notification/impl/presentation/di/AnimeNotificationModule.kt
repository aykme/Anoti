package com.alekseivinogradov.anime_notification.impl.presentation.di

import android.content.Context
import com.alekseivinogradov.anime_notification.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anime_notification.impl.presentation.manager.AnimeNotificationManagerImpl
import com.alekseivinogradov.anime_notification.impl.presentation.provider.AnimeNotificationIntentProvider
import com.alekseivinogradov.di.api.presentation.AppContext
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
interface AnimeNotificationModule {
    companion object {
        @Provides
        @Singleton
        fun provideAnimeNotificationManager(
            @AppContext
            appContext: Context,
            animeNotificationIntentProvider: AnimeNotificationIntentProvider
        ): AnimeNotificationManager {
            return AnimeNotificationManagerImpl(
                appContext = appContext,
                animeNotificationIntentProvider = animeNotificationIntentProvider
            )
        }
    }
}
