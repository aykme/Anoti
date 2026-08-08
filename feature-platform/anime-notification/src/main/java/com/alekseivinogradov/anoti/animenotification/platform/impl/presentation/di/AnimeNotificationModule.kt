package com.alekseivinogradov.anoti.animenotification.platform.impl.presentation.di

import android.content.Context
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.animenotification.platform.impl.presentation.manager.AnimeNotificationManagerImpl
import com.alekseivinogradov.anoti.animenotification.external.platform.impl.presentation.provider.AnimeNotificationIntentProvider
import com.alekseivinogradov.anoti.di.platform.api.presentation.AppContext
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
