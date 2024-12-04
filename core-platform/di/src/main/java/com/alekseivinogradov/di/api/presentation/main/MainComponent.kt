package com.alekseivinogradov.di.api.presentation.main

import android.content.Context
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.api.domain.formatter.DateFormatter
import com.alekseivinogradov.celebrity.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.di.api.presentation.ActivityContext
import com.alekseivinogradov.di.api.presentation.AppContext
import com.arkivanov.mvikotlin.core.store.StoreFactory

interface MainComponent {
    @AppContext
    fun provideAppContext(): Context

    @ActivityContext
    fun provideActivityContext(): Context

    fun provideStoreFactory(): StoreFactory

    fun provideCoroutineContextProvider(): CoroutineContextProvider

    fun provideToastProvider(): ToastProvider

    fun provideAnimeDatabaseStore(): AnimeDatabaseStore

    fun provideShikimoriApiService(): ShikimoriApiService

    fun provideDateFormatter(): DateFormatter
}
