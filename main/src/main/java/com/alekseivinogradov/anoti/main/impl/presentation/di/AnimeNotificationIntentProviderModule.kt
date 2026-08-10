package com.alekseivinogradov.anoti.main.impl.presentation.di

import com.alekseivinogradov.anoti.animenotification.external.android.impl.presentation.provider.AnimeNotificationIntentProvider
import com.alekseivinogradov.anoti.main.impl.presentation.provider.AnimeNotificationIntentProviderImpl
import dagger.Module
import dagger.Provides

@Module
interface AnimeNotificationIntentProviderModule {
    companion object {
        @Provides
        fun provideAnimeNotificationIntentProvider(
            impl: AnimeNotificationIntentProviderImpl
        ): AnimeNotificationIntentProvider = impl
    }
}
