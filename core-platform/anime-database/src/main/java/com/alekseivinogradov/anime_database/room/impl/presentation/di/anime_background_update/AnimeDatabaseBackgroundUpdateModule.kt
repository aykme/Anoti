package com.alekseivinogradov.anime_database.room.impl.presentation.di.anime_background_update

import com.alekseivinogradov.anime_database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anime_database.api.domain.usecase.FetchAllAnimeDatabaseItemsUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.UpdateAnimeDatabaseItemUsecase
import com.alekseivinogradov.anime_database.impl.domain.usecase.FetchAllAnimeDatabaseItemsUsecaseImpl
import com.alekseivinogradov.anime_database.impl.domain.usecase.UpdateAnimeDatabaseItemUsecaseImpl
import dagger.Module
import dagger.Provides

@Module
interface AnimeDatabaseBackgroundUpdateModule {
    companion object {
        @Provides
        fun provideUpdateAnimeDatabaseItemUsecase(
            repository: AnimeDatabaseRepository
        ): UpdateAnimeDatabaseItemUsecase = UpdateAnimeDatabaseItemUsecaseImpl(repository)

        @Provides
        fun provideFetchAllAnimeDatabaseItemsUsecase(
            repository: AnimeDatabaseRepository
        ): FetchAllAnimeDatabaseItemsUsecase = FetchAllAnimeDatabaseItemsUsecaseImpl(repository)
    }
}
