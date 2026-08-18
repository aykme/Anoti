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

    // Deliberately unscoped: DateFormatterImpl is stateless, so every injection point gets its
    // own instance instead of sharing one for the app's lifetime.
    @Provides
    fun provideDateFormatter(): DateFormatter = DateFormatterImpl()
}
