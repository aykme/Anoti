package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.di

import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.domain.store.BottomNavigationBarStoreFactory
import com.arkivanov.mvikotlin.core.store.StoreFactory
import me.tatarka.inject.annotations.Provides

/** Provides the [BottomNavigationBarStore] binding; mixed into `main`'s `DiRootComponent`. */
interface DiBottomNavigationBarComponent {
    @Provides
    fun provideBottomNavigationBarStore(storeFactory: StoreFactory): BottomNavigationBarStore =
        BottomNavigationBarStoreFactory(storeFactory).create()
}
