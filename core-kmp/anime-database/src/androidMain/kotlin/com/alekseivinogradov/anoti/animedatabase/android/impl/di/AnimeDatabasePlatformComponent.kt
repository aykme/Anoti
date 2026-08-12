package com.alekseivinogradov.anoti.animedatabase.android.impl.di

import android.content.Context
import com.alekseivinogradov.anoti.animedatabase.android.impl.data.getAnimeDatabase
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.AnimeDatabase
import com.alekseivinogradov.anoti.di.kmp.PlatformContext
import com.alekseivinogradov.anoti.di.kmp.qualifier.AppContext
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@ContributesTo(AppScope::class)
interface AnimeDatabasePlatformComponent {
    @Provides
    @SingleIn(AppScope::class)
    fun provideAnimeDatabase(@AppContext appContext: PlatformContext): AnimeDatabase =
        getAnimeDatabase(appContext as Context)
}
