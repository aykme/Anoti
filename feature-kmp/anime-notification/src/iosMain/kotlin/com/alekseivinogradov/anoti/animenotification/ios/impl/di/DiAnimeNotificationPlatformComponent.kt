package com.alekseivinogradov.anoti.animenotification.ios.impl.di

import coil3.PlatformContext
import com.alekseivinogradov.anoti.animenotification.external.kmp.api.presentation.provider.AnimeNotificationTapPayloadProvider
import com.alekseivinogradov.anoti.animenotification.ios.impl.presentation.manager.AnimeNotificationManagerImpl
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.poster.PosterLoader
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import me.tatarka.inject.annotations.Provides

/**
 * Provides the iOS [AnimeNotificationManager] binding; mixed into `core-kmp:di-app`'s iOS
 * `DiAppComponent`. [AnimeNotificationTapPayloadProvider] is consumed, not provided. The module
 * owning the screens (`main`) contributes it.
 */
interface DiAnimeNotificationPlatformComponent {
    @Provides
    @AppScope
    fun provideAnimeNotificationManager(
        coroutineContextProvider: CoroutineContextProvider,
        animeNotificationTapPayloadProvider: AnimeNotificationTapPayloadProvider
    ): AnimeNotificationManager = AnimeNotificationManagerImpl(
        coroutineContextProvider = coroutineContextProvider,
        posterLoader = PosterLoader(platformContext = PlatformContext.INSTANCE),
        animeNotificationTapPayloadProvider = animeNotificationTapPayloadProvider
    )
}
