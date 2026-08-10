package com.alekseivinogradov.anoti.di.android.api.presentation.main

import android.content.Context
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.celebrity.android.api.presentation.di.ActivityContext
import com.alekseivinogradov.anoti.celebrity.android.api.presentation.di.AppContext
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory

/**
 * The activity-scoped Dagger component contract for "main"'s screen graph. Implemented by
 * `:main`'s own internal component (which adds the actual `@Component` annotation, its
 * `AppComponent` dependency, and module list); feature modules depend on this interface only.
 */
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

    fun provideSafeApi(): SafeApi

    fun provideDateFormatter(): DateFormatter
}
