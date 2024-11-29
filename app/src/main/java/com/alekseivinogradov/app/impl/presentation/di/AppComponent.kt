package com.alekseivinogradov.app.impl.presentation.di

import android.content.Context
import com.alekseivinogradov.app.impl.presentation.AnotiApp
import com.alekseivinogradov.celebrity.impl.presentation.di.AppContext
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Component(modules = [AppModule::class])
@Singleton
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance
            @AppContext
            appContext: Context
        ): AppComponent
    }

    fun inject(app: AnotiApp)

    @AppContext
    @Singleton
    fun provideAppContext(): Context
}
