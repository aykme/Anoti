package com.alekseivinogradov.main.impl.presentation.di

import com.alekseivinogradov.bottom_navigation_bar.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.bottom_navigation_bar.impl.domain.store.BottomNavigationBarStoreFactory
import com.alekseivinogradov.celebrity.impl.domain.coroutine_context.CelebrityModule
import com.alekseivinogradov.anime_database.room.impl.presentation.di.AnimeDatabaseCompletedModule
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dagger.Module
import dagger.Provides

@Module(
    includes = [
        CelebrityModule::class,
        AnimeDatabaseCompletedModule::class
    ]
)
interface MainModule {
    companion object {
        @Provides
        fun provideBottomNavigationBarStore(
            storeFactory: StoreFactory
        ): BottomNavigationBarStore = BottomNavigationBarStoreFactory(storeFactory).create()
    }
}
