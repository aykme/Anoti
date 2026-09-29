package com.alekseivinogradov.anoti.main.impl.presentation.di

import com.alekseivinogradov.anoti.animenotification.external.kmp.api.presentation.provider.AnimeNotificationTapPayloadProvider
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.alekseivinogradov.anoti.main.impl.presentation.provider.AnimeNotificationTapPayloadProviderImpl
import me.tatarka.inject.annotations.Provides

/**
 * The `main`-side binding of the notification tap payload, for an app-wide component whose
 * notifications carry their tap target as data. The iOS component mixes it in.
 */
interface DiRootNotificationTapComponent {
    @Provides
    @AppScope
    fun provideAnimeNotificationTapPayloadProvider(
        impl: AnimeNotificationTapPayloadProviderImpl
    ): AnimeNotificationTapPayloadProvider = impl
}
