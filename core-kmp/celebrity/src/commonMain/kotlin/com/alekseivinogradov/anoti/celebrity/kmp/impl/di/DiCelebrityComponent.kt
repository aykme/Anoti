package com.alekseivinogradov.anoti.celebrity.kmp.impl.di

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.formatter.DateFormatterImpl
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import me.tatarka.inject.annotations.Provides

/**
 * Provides the app-wide [StoreFactory] and [DateFormatter] bindings; mixed into `DiAppComponent`
 * on both platforms. Both are already multiplatform, so unlike the rest of this module's DI,
 * these bindings are platform-independent and live directly in commonMain.
 */
interface DiCelebrityComponent {
    @Provides
    @AppScope
    fun provideStoreFactory(): StoreFactory = DefaultStoreFactory()

    // App-scoped rather than root-scoped on purpose: the provider is unscoped, so every
    // injection point still gets its own stateless DateFormatterImpl, and keeping the binding
    // in AppScope is what lets iOS reach it as well — iOS has no RootScope graph.
    @Provides
    fun provideDateFormatter(): DateFormatter = DateFormatterImpl()
}
