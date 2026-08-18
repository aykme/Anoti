package com.alekseivinogradov.anoti.animedatabase.ios.impl.di

import com.alekseivinogradov.anoti.animedatabase.ios.impl.data.getAnimeDatabase
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.AnimeDatabase
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * Contributes the iOS [AnimeDatabase] binding to [AppScope]'s merged component.
 */
@ContributesTo(AppScope::class)
interface DiAnimeDatabasePlatformComponent {
    @Provides
    @SingleIn(AppScope::class)
    fun provideAnimeDatabase(): AnimeDatabase = getAnimeDatabase()
}
