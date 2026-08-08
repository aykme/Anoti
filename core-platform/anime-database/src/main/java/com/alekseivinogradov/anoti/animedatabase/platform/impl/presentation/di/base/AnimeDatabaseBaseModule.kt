package com.alekseivinogradov.anoti.animedatabase.platform.impl.presentation.di.base

import android.content.Context
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anoti.animedatabase.platform.impl.data.AnimeDatabase
import com.alekseivinogradov.anoti.animedatabase.platform.impl.data.repository.AnimeDatabaseRepositoryImpl
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
        ): AnimeDatabase = AnimeDatabase.getDatabase(appContext)

        @Provides
        @Singleton
        fun provideAnimeDatabaseRepository(
            animeDatabase: AnimeDatabase
        ): AnimeDatabaseRepository =
            AnimeDatabaseRepositoryImpl(animeDao = animeDatabase.animeDao())
    }
}
