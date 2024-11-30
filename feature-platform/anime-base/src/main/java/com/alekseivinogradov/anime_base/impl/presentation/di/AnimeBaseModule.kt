package com.alekseivinogradov.anime_base.impl.presentation.di

import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiServicePlatform
import com.alekseivinogradov.anime_base.impl.data.service.ShikimoriApiServiceImpl
import dagger.Module
import dagger.Provides

@Module
interface AnimeBaseModule {
    companion object {
        @Provides
        fun provideShikimoriApiService(
            servicePlatform: ShikimoriApiServicePlatform
        ): ShikimoriApiService = ShikimoriApiServiceImpl(servicePlatform)

        @Provides
        fun provideShikimoriServicePlatform(): ShikimoriApiServicePlatform =
            ShikimoriApiServicePlatform.instance
    }
}
