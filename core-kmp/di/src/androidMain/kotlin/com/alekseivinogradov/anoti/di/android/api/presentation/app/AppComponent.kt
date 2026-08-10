package com.alekseivinogradov.anoti.di.android.api.presentation.app

import android.content.Context
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.celebrity.android.api.presentation.di.AppContext
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory

/**
 * The application-scoped Dagger component contract. Implemented by `:app`'s own internal
 * component (which adds the actual `@Component` annotation and module list); other modules
 * depend on this interface only, never on the concrete implementation.
 */
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
