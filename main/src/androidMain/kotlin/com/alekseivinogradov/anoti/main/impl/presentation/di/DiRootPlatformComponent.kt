package com.alekseivinogradov.anoti.main.impl.presentation.di

import com.alekseivinogradov.anoti.animenotification.external.android.impl.presentation.provider.AnimeNotificationIntentProvider
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.alekseivinogradov.anoti.main.impl.presentation.provider.AnimeNotificationIntentProviderImpl
import me.tatarka.inject.annotations.Provides

/**
 * The `main`-side Android bindings for the app-wide component: the notification intent provider,
 * which needs both the root navigation config and `MainActivity`.
 */
interface DiRootPlatformComponent {
    @Provides
    @AppScope
    fun provideAnimeNotificationIntentProvider(
        impl: AnimeNotificationIntentProviderImpl
    ): AnimeNotificationIntentProvider = impl
}
