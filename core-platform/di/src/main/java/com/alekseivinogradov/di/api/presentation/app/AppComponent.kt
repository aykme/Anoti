package com.alekseivinogradov.di.api.presentation.app

import android.content.Context
import com.alekseivinogradov.di.api.presentation.AppContext
import com.arkivanov.mvikotlin.core.store.StoreFactory

interface AppComponent {
    @AppContext
    fun provideAppContextFromApplication(): Context

    fun provideStoreFactory(): StoreFactory
}
