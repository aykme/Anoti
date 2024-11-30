package com.alekseivinogradov.main.impl.presentation.di

import android.content.Context
import com.alekseivinogradov.di.api.presentation.AppContext
import com.alekseivinogradov.di.api.presentation.app.AppComponent
import com.alekseivinogradov.di.api.presentation.main.MainComponent
import com.alekseivinogradov.main.impl.presentation.MainActivity
import dagger.Component
import javax.inject.Singleton

@Component(
    dependencies = [AppComponent::class],
    modules = [MainModule::class]
)
@Singleton
interface MainComponentInternal : MainComponent {

    @Component.Factory
    interface Factory {
        fun create(appComponent: AppComponent): MainComponentInternal
    }

    fun inject(activity: MainActivity)

    @AppContext
    override fun provideAppContextFromActivity(): Context
}
