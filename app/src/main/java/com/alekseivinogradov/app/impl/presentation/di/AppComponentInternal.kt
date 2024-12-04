package com.alekseivinogradov.app.impl.presentation.di

import android.content.Context
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.app.impl.presentation.AnotiApp
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.di.api.presentation.AppContext
import com.alekseivinogradov.di.api.presentation.app.AppComponent
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
}
