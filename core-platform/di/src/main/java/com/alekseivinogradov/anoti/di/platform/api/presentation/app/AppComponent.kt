package com.alekseivinogradov.anoti.di.platform.api.presentation.app

import android.content.Context
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.di.platform.api.presentation.AppContext
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory

interface AppComponent {
    @AppContext
    fun provideAppContext(): Context

    fun provideStoreFactory(): StoreFactory

    fun provideCoroutineContextProvider(): CoroutineContextProvider

    fun provideToastProvider(): ToastProvider

    fun provideAnimeDatabaseStore(): AnimeDatabaseStore

    fun provideShikimoriApiService(): ShikimoriApiService

    fun provideSafeApi(): SafeApi
}
