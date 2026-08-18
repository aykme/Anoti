package com.alekseivinogradov.anoti.animenotification.ios.impl.di

import com.alekseivinogradov.anoti.animenotification.ios.impl.presentation.manager.AnimeNotificationManagerImpl
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import me.tatarka.inject.annotations.Provides

/**
 * Provides the iOS [AnimeNotificationManager] binding; mixed into `core-kmp:di`'s
 * `DiAppComponent`.
 */
interface DiAnimeNotificationPlatformComponent {
    @Provides
    @AppScope
    fun provideAnimeNotificationManager(
        coroutineContextProvider: CoroutineContextProvider
    ): AnimeNotificationManager = AnimeNotificationManagerImpl(coroutineContextProvider)
}
