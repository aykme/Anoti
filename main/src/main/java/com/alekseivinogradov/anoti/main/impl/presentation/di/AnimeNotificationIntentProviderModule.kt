package com.alekseivinogradov.anoti.main.impl.presentation.di

import com.alekseivinogradov.anoti.animenotification.external.android.impl.presentation.provider.AnimeNotificationIntentProvider
import com.alekseivinogradov.anoti.main.impl.presentation.provider.AnimeNotificationIntentProviderImpl
import dagger.Binds
import dagger.Module

@Module
interface AnimeNotificationIntentProviderModule {
    @Binds
    fun bindAnimeNotificationIntentProvider(
        impl: AnimeNotificationIntentProviderImpl
    ): AnimeNotificationIntentProvider
}
