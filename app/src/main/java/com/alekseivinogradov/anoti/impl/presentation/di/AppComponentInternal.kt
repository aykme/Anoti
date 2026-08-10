package com.alekseivinogradov.anoti.impl.presentation.di

import android.content.Context
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.impl.presentation.AnotiApp
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.di.android.api.presentation.AppContext
import com.alekseivinogradov.anoti.di.android.api.presentation.app.AppComponent
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Component(modules = [AppModule::class])
@Singleton
interface AppComponentInternal : AppComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance
            @AppContext
            appContext: Context
        ): AppComponentInternal
    }

    fun inject(app: AnotiApp)

    @AppContext
    override fun provideAppContext(): Context

    override fun provideStoreFactory(): StoreFactory

    override fun provideCoroutineContextProvider(): CoroutineContextProvider

    override fun provideToastProvider(): ToastProvider

    override fun provideAnimeDatabaseStore(): AnimeDatabaseStore

    override fun provideShikimoriApiService(): ShikimoriApiService

    override fun provideSafeApi(): SafeApi
}
