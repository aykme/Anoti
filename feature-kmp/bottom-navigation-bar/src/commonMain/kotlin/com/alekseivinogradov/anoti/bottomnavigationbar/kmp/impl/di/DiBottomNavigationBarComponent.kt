package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.di

import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.domain.store.BottomNavigationBarStoreFactory
import com.alekseivinogradov.anoti.di.kmp.scope.RootScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import me.tatarka.inject.annotations.Provides

/** Contributes the [BottomNavigationBarStore] binding to [RootScope]'s merged component. */
interface DiBottomNavigationBarComponent {
    @Provides
    fun provideBottomNavigationBarStore(storeFactory: StoreFactory): BottomNavigationBarStore =
        BottomNavigationBarStoreFactory(storeFactory).create()
}
