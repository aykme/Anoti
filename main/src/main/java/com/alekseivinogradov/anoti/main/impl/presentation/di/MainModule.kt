package com.alekseivinogradov.anoti.main.impl.presentation.di

import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.domain.store.BottomNavigationBarStoreFactory
import com.alekseivinogradov.anoti.celebrity.android.impl.presentation.di.CelebrityActivityModule
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
