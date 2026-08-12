package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.di

import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.domain.store.BottomNavigationBarStoreFactory
import com.alekseivinogradov.anoti.di.kmp.scope.ActivityScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo

/** Contributes the [BottomNavigationBarStore] binding to [ActivityScope]'s merged component. */
@ContributesTo(ActivityScope::class)
interface BottomNavigationBarComponent {
    @Provides
    fun provideBottomNavigationBarStore(storeFactory: StoreFactory): BottomNavigationBarStore =
        BottomNavigationBarStoreFactory(storeFactory).create()
}
