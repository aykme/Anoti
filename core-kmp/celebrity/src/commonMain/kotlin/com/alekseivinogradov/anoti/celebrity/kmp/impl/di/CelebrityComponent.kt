package com.alekseivinogradov.anoti.celebrity.kmp.impl.di

import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * Contributes the app-wide [StoreFactory] binding to [AppScope]'s merged component. MVIKotlin is
 * already multiplatform, so unlike the rest of this module's DI, this binding is
 * platform-independent and lives directly in commonMain.
 */
@ContributesTo(AppScope::class)
interface CelebrityComponent {
    @Provides
    @SingleIn(AppScope::class)
    fun provideStoreFactory(): StoreFactory = DefaultStoreFactory()
}
