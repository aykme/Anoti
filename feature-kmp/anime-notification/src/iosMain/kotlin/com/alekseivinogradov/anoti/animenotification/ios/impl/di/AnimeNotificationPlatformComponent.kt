package com.alekseivinogradov.anoti.animenotification.ios.impl.di

import com.alekseivinogradov.anoti.animenotification.ios.impl.presentation.manager.AnimeNotificationManagerImpl
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/** Contributes the iOS [AnimeNotificationManager] binding to [AppScope]'s merged component. */
@ContributesTo(AppScope::class)
interface AnimeNotificationPlatformComponent {
    @Provides
    @SingleIn(AppScope::class)
    fun provideAnimeNotificationManager(
        coroutineContextProvider: CoroutineContextProvider
    ): AnimeNotificationManager = AnimeNotificationManagerImpl(coroutineContextProvider)
}
