package com.alekseivinogradov.anoti.animedatabase.ios.impl.di

import com.alekseivinogradov.anoti.animedatabase.ios.impl.data.getAnimeDatabase
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.AnimeDatabase
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@ContributesTo(AppScope::class)
interface AnimeDatabasePlatformComponent {
    @Provides
    @SingleIn(AppScope::class)
    fun provideAnimeDatabase(): AnimeDatabase = getAnimeDatabase()
}
