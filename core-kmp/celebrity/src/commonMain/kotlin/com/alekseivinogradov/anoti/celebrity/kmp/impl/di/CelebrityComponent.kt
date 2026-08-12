package com.alekseivinogradov.anoti.celebrity.kmp.impl.di

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.formatter.DateFormatterImpl
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * Contributes the app-wide [StoreFactory] and [DateFormatter] bindings to [AppScope]'s merged
 * component. Both are already multiplatform, so unlike the rest of this module's DI, these
 * bindings are platform-independent and live directly in commonMain.
 */
@ContributesTo(AppScope::class)
interface CelebrityComponent {
    @Provides
    @SingleIn(AppScope::class)
    fun provideStoreFactory(): StoreFactory = DefaultStoreFactory()

    // App-scoped rather than activity-scoped on purpose: the provider is unscoped, so every
    // injection point still gets its own stateless DateFormatterImpl, and keeping the binding
    // in AppScope is what lets iOS reach it as well — iOS has no ActivityScope graph.
    @Provides
    fun provideDateFormatter(): DateFormatter = DateFormatterImpl()
}
