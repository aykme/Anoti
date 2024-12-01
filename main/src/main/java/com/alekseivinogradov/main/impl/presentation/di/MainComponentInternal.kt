package com.alekseivinogradov.main.impl.presentation.di

import android.content.Context
import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.di.api.presentation.AppContext
import com.alekseivinogradov.di.api.presentation.app.AppComponent
import com.alekseivinogradov.di.api.presentation.main.MainComponent
import com.alekseivinogradov.di.api.presentation.scope.ActivityScope
import com.alekseivinogradov.main.impl.presentation.MainActivity
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dagger.Component

@Component(
    dependencies = [AppComponent::class],
    modules = [MainModule::class]
)
@ActivityScope
interface MainComponentInternal : MainComponent {

    @Component.Factory
    interface Factory {
        fun create(appComponent: AppComponent): MainComponentInternal
    }

    fun inject(activity: MainActivity)

    @AppContext
    override fun provideAppContext(): Context

    override fun provideStoreFactory(): StoreFactory

    override fun provideCoroutineContextProvider(): CoroutineContextProvider

    override fun provideAnimeDatabaseStore(): AnimeDatabaseStore
}
