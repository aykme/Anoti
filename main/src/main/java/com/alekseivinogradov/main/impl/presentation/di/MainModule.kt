package com.alekseivinogradov.main.impl.presentation.di

import com.alekseivinogradov.bottom_navigation_bar.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.bottom_navigation_bar.impl.domain.store.BottomNavigationBarStoreFactory
import com.alekseivinogradov.celebrity.impl.presentation.di.CelebrityActivityModule
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dagger.Module
import dagger.Provides

@Module(includes = [CelebrityActivityModule::class])
interface MainModule {
    companion object {
        @Provides
        fun provideBottomNavigationBarStore(
            storeFactory: StoreFactory
        ): BottomNavigationBarStore = BottomNavigationBarStoreFactory(storeFactory).create()
    }
}
