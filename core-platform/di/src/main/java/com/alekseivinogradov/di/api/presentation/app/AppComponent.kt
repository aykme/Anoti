package com.alekseivinogradov.di.api.presentation.app

import android.content.Context
import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.di.api.presentation.AppContext
import com.arkivanov.mvikotlin.core.store.StoreFactory

interface AppComponent {
    @AppContext
    fun provideAppContext(): Context

    fun provideStoreFactory(): StoreFactory

    fun provideCoroutineContextProvider(): CoroutineContextProvider

    fun provideAnimeDatabaseStore(): AnimeDatabaseStore
}
