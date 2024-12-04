package com.alekseivinogradov.celebrity.impl.presentation.di

import android.content.Context
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.api.domain.toast.provider.MakeConnectionErrorToast
import com.alekseivinogradov.celebrity.api.domain.toast.provider.MakeUnknownErrorToast
import com.alekseivinogradov.celebrity.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.celebrity.impl.domain.coroutine_context.CoroutineContextProviderPlatform
import com.alekseivinogradov.celebrity.impl.presentation.toast.manager.ToastManager
import com.alekseivinogradov.di.api.presentation.AppContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
interface CelebrityAppModule {
    @Binds
    fun bindCoroutineContextProvider(
        impl: CoroutineContextProviderPlatform
    ): CoroutineContextProvider

    companion object {
        @Provides
        @Singleton
        fun provideStoreFactory(): StoreFactory = DefaultStoreFactory()

        @Provides
        @Singleton
        @ConnectionError
        fun provideMakeConnectionErrorToast(
            @AppContext appContext: Context
        ): MakeConnectionErrorToast = { ToastManager.makeConnectionErrorToast(appContext) }

        @Provides
        @Singleton
        @UnknownError
        fun provideMakeUnknownErrorToast(
            @AppContext appContext: Context
        ): MakeUnknownErrorToast = { ToastManager.makeUnknownErrorToast(appContext) }

        @Provides
        @Singleton
        fun provideToastProvider(
            @ConnectionError makeConnectionErrorToast: MakeConnectionErrorToast,
            @UnknownError makeUnknownErrorToast: MakeUnknownErrorToast
        ): ToastProvider {
            return ToastProvider(
                makeConnectionErrorToast = makeConnectionErrorToast,
                makeUnknownErrorToast = makeUnknownErrorToast
            )
        }
    }
}

@Qualifier
annotation class ConnectionError

@Qualifier
annotation class UnknownError
