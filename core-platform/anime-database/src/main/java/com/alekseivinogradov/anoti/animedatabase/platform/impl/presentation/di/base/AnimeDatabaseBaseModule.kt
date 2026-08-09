package com.alekseivinogradov.anoti.animedatabase.platform.impl.presentation.di.base

import android.content.Context
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.AnimeDatabase
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.getAnimeDatabase
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.repository.AnimeDatabaseRepositoryImpl
import com.alekseivinogradov.anoti.di.platform.api.presentation.AppContext
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
interface AnimeDatabaseBaseModule {
    companion object {
        @Provides
        @Singleton
        fun provideAnimeDatabase(
            @AppContext
            appContext: Context
        ): AnimeDatabase = getAnimeDatabase(appContext)

        @Provides
        @Singleton
        fun provideAnimeDatabaseRepository(
            animeDatabase: AnimeDatabase
        ): AnimeDatabaseRepository =
            AnimeDatabaseRepositoryImpl(animeDao = animeDatabase.animeDao())
    }
}
