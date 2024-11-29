package com.alekseivinogradov.app.impl.presentation.di

import android.content.Context
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiServicePlatform
import com.alekseivinogradov.anime_base.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.impl.domain.coroutine_context.CoroutineContextProviderPlatform
import com.alekseivinogradov.celebrity.impl.presentation.di.AppContext
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal interface AppModule {
    companion object {
        @Provides
        @Singleton
        fun provideCoroutineContextProvider(
            @AppContext
            appContext: Context
        ): CoroutineContextProvider = CoroutineContextProviderPlatform(appContext)

        @Provides
        @Singleton
        fun provideShikimoriServicePlatform(): ShikimoriApiServicePlatform =
            ShikimoriApiServicePlatform.instance

        @Provides
        @Singleton
        fun provideShikimoriApiService(
            servicePlatform: ShikimoriApiServicePlatform
        ): ShikimoriApiService = ShikimoriApiServiceImpl(servicePlatform)
    }
}
