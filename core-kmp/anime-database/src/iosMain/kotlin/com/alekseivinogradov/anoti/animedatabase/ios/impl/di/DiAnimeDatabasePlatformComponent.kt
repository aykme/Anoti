package com.alekseivinogradov.anoti.animedatabase.ios.impl.di

import com.alekseivinogradov.anoti.animedatabase.ios.impl.data.getAnimeDatabase
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.AnimeDatabase
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import me.tatarka.inject.annotations.Provides

/**
 * Contributes the iOS [AnimeDatabase] binding to [AppScope]'s merged component.
 */
interface DiAnimeDatabasePlatformComponent {
    @Provides
    @AppScope
    fun provideAnimeDatabase(): AnimeDatabase = getAnimeDatabase()
}
