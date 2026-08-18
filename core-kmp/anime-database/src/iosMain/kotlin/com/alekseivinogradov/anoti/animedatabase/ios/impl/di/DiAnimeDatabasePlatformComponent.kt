package com.alekseivinogradov.anoti.animedatabase.ios.impl.di

import com.alekseivinogradov.anoti.animedatabase.ios.impl.data.getAnimeDatabase
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.AnimeDatabase
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import me.tatarka.inject.annotations.Provides

/**
 * Provides the iOS [AnimeDatabase] binding; mixed into `core-kmp:di`'s `DiAppComponent`.
 */
interface DiAnimeDatabasePlatformComponent {
    @Provides
    @AppScope
    fun provideAnimeDatabase(): AnimeDatabase = getAnimeDatabase()
}
