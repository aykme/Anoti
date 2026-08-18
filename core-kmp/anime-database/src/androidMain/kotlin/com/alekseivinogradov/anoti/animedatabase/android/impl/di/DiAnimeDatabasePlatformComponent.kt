package com.alekseivinogradov.anoti.animedatabase.android.impl.di

import com.alekseivinogradov.anoti.animedatabase.android.impl.data.getAnimeDatabase
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.AnimeDatabase
import com.alekseivinogradov.anoti.di.kmp.PlatformContext
import com.alekseivinogradov.anoti.di.kmp.qualifier.AppContext
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import me.tatarka.inject.annotations.Provides

/**
 * Contributes the Android [AnimeDatabase] binding to [AppScope]'s merged component.
 */
interface DiAnimeDatabasePlatformComponent {
    @Provides
    @AppScope
    fun provideAnimeDatabase(@AppContext appContext: PlatformContext): AnimeDatabase =
        getAnimeDatabase(appContext)
}
