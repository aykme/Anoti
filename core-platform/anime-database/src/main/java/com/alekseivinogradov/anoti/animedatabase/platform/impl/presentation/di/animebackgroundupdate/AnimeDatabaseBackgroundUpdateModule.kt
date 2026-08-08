package com.alekseivinogradov.anoti.animedatabase.platform.impl.presentation.di.animebackgroundupdate

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.FetchAllAnimeDatabaseItemsUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.UpdateAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.FetchAllAnimeDatabaseItemsUsecaseImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.UpdateAnimeDatabaseItemUsecaseImpl
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
