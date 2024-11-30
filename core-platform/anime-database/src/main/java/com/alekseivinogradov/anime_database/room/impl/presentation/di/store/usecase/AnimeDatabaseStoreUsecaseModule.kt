package com.alekseivinogradov.anime_database.room.impl.presentation.di.store.usecase

import com.alekseivinogradov.anime_database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anime_database.api.domain.usecase.ChangeAnimeDatabaseItemNewEpisodeStatusUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.DeleteAnimeDatabaseItemUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.FetchAllAnimeDatabaseItemsFlowUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.InsertAnimeDatabaseItemUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.UpdateAnimeDatabaseItemUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.wrapper.AnimeDatabaseUsecases
import com.alekseivinogradov.anime_database.impl.domain.usecase.ChangeAnimeDatabaseItemNewEpisodeStatusUsecaseImpl
import com.alekseivinogradov.anime_database.impl.domain.usecase.DeleteAnimeDatabaseItemUsecaseImpl
import com.alekseivinogradov.anime_database.impl.domain.usecase.FetchAllAnimeDatabaseItemsFlowUsecaseImpl
import com.alekseivinogradov.anime_database.impl.domain.usecase.InsertAnimeDatabaseItemUsecaseImpl
import com.alekseivinogradov.anime_database.impl.domain.usecase.ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecaseImpl
import com.alekseivinogradov.anime_database.room.impl.presentation.di.anime_background_update.AnimeDatabaseBackgroundUpdateModule
import dagger.Module
import dagger.Provides

@Module(includes = [AnimeDatabaseBackgroundUpdateModule::class])
interface AnimeDatabaseStoreUsecaseModule {
    companion object {
        @Provides
        fun provideFetchAllAnimeDatabaseItemsFlowUsecase(
            repository: AnimeDatabaseRepository
        ): FetchAllAnimeDatabaseItemsFlowUsecase =
            FetchAllAnimeDatabaseItemsFlowUsecaseImpl(repository)

        @Provides
        fun provideInsertAnimeDatabaseItemUsecase(
            repository: AnimeDatabaseRepository
        ): InsertAnimeDatabaseItemUsecase = InsertAnimeDatabaseItemUsecaseImpl(repository)

        @Provides
        fun provideDeleteAnimeDatabaseItemUsecase(
            repository: AnimeDatabaseRepository
        ): DeleteAnimeDatabaseItemUsecase = DeleteAnimeDatabaseItemUsecaseImpl(repository)

        @Provides
        fun provideResetAllAnimeDatabaseItemsNewEpisodeStatusUsecase(
            repository: AnimeDatabaseRepository
        ): ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecase =
            ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecaseImpl(repository)

        @Provides
        fun provideChangeAnimeDatabaseItemNewEpisodeStatusUsecase(
            repository: AnimeDatabaseRepository
        ): ChangeAnimeDatabaseItemNewEpisodeStatusUsecase =
            ChangeAnimeDatabaseItemNewEpisodeStatusUsecaseImpl(repository)

        @Provides
        fun provideAnimeDatabaseUsecases(
            fetchAllAnimeDatabaseItemsFlowUsecase: FetchAllAnimeDatabaseItemsFlowUsecase,
            insertAnimeDatabaseItemUsecase: InsertAnimeDatabaseItemUsecase,
            deleteAnimeDatabaseItemUsecase: DeleteAnimeDatabaseItemUsecase,
            resetAllAnimeDatabaseItemsNewEpisodeStatusUsecase:
            ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecase,
            changeAnimeDatabaseItemNewEpisodeStatusUsecase:
            ChangeAnimeDatabaseItemNewEpisodeStatusUsecase,
            updateAnimeDatabaseItemUsecase: UpdateAnimeDatabaseItemUsecase
        ): AnimeDatabaseUsecases {
            return AnimeDatabaseUsecases(
                fetchAllAnimeDatabaseItemsFlowUsecase = fetchAllAnimeDatabaseItemsFlowUsecase,
                insertAnimeDatabaseItemUsecase = insertAnimeDatabaseItemUsecase,
                deleteAnimeDatabaseItemUsecase = deleteAnimeDatabaseItemUsecase,
                resetAllAnimeDatabaseItemsNewEpisodeStatusUsecase =
                resetAllAnimeDatabaseItemsNewEpisodeStatusUsecase,
                changeAnimeDatabaseItemNewEpisodeStatusUsecase =
                changeAnimeDatabaseItemNewEpisodeStatusUsecase,
                updateAnimeDatabaseItemUsecase = updateAnimeDatabaseItemUsecase
            )
        }
    }
}
