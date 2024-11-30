package com.alekseivinogradov.anime_database.room.impl.presentation.di.base

import android.content.Context
import com.alekseivinogradov.anime_database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anime_database.room.impl.data.AnimeDatabase
import com.alekseivinogradov.anime_database.room.impl.data.repository.AnimeDatabaseRepositoryImpl
import com.alekseivinogradov.di.api.presentation.AppContext
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
