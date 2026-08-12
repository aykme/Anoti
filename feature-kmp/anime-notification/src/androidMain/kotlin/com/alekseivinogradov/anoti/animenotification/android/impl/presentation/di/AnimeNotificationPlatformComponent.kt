package com.alekseivinogradov.anoti.animenotification.android.impl.presentation.di

import android.content.Context
import com.alekseivinogradov.anoti.animenotification.android.impl.presentation.manager.AnimeNotificationManagerImpl
import com.alekseivinogradov.anoti.animenotification.external.android.impl.presentation.provider.AnimeNotificationIntentProvider
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.di.kmp.PlatformContext
import com.alekseivinogradov.anoti.di.kmp.qualifier.AppContext
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * Contributes the Android [AnimeNotificationManager] binding to [AppScope]'s merged component.
 * [AnimeNotificationIntentProvider] is consumed, not provided — the module owning the navigation
 * graph and the target activity (`main`) contributes its implementation.
 */
@ContributesTo(AppScope::class)
interface AnimeNotificationPlatformComponent {
    @Provides
    @SingleIn(AppScope::class)
    fun provideAnimeNotificationManager(
        @AppContext appContext: PlatformContext,
        animeNotificationIntentProvider: AnimeNotificationIntentProvider,
        coroutineContextProvider: CoroutineContextProvider
    ): AnimeNotificationManager = AnimeNotificationManagerImpl(
        appContext = appContext,
        animeNotificationIntentProvider = animeNotificationIntentProvider,
        coroutineContextProvider = coroutineContextProvider
    )
}
