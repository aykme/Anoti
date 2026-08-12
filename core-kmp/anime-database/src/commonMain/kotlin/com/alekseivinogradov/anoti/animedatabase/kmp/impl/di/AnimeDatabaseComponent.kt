package com.alekseivinogradov.anoti.animedatabase.kmp.impl.di

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.ChangeAnimeDatabaseItemNewEpisodeStatusUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.DeleteAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.FetchAllAnimeDatabaseItemsFlowUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.FetchAllAnimeDatabaseItemsUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.InsertAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.UpdateAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.wrapper.AnimeDatabaseUsecases
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.AnimeDatabase
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.repository.AnimeDatabaseRepositoryImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.AnimeDatabaseExecutorImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.AnimeDatabaseStoreFactory
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.ChangeAnimeDatabaseItemNewEpisodeStatusUsecaseImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.DeleteAnimeDatabaseItemUsecaseImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.FetchAllAnimeDatabaseItemsFlowUsecaseImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.FetchAllAnimeDatabaseItemsUsecaseImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.InsertAnimeDatabaseItemUsecaseImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecaseImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.UpdateAnimeDatabaseItemUsecaseImpl
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * Contributes the local anime database's [AnimeDatabaseRepository], usecase, and
 * [AnimeDatabaseStore] bindings to [AppScope]'s merged component.
 */
@ContributesTo(AppScope::class)
interface AnimeDatabaseComponent {
    @Provides
    @SingleIn(AppScope::class)
    fun provideAnimeDatabaseRepository(animeDatabase: AnimeDatabase): AnimeDatabaseRepository =
        AnimeDatabaseRepositoryImpl(animeDao = animeDatabase.animeDao())

    @Provides
    fun provideFetchAllAnimeDatabaseItemsUsecase(
        repository: AnimeDatabaseRepository
    ): FetchAllAnimeDatabaseItemsUsecase = FetchAllAnimeDatabaseItemsUsecaseImpl(repository)

    @Provides
    fun provideUpdateAnimeDatabaseItemUsecase(
        repository: AnimeDatabaseRepository
    ): UpdateAnimeDatabaseItemUsecase = UpdateAnimeDatabaseItemUsecaseImpl(repository)

    @Provides
    fun provideFetchAllAnimeDatabaseItemsFlowUsecase(
        repository: AnimeDatabaseRepository
    ): FetchAllAnimeDatabaseItemsFlowUsecase = FetchAllAnimeDatabaseItemsFlowUsecaseImpl(repository)

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
        resetAllAnimeDatabaseItemsNewEpisodeStatusUsecase: ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecase,
        changeAnimeDatabaseItemNewEpisodeStatusUsecase: ChangeAnimeDatabaseItemNewEpisodeStatusUsecase,
        updateAnimeDatabaseItemUsecase: UpdateAnimeDatabaseItemUsecase
    ): AnimeDatabaseUsecases = AnimeDatabaseUsecases(
        fetchAllAnimeDatabaseItemsFlowUsecase = fetchAllAnimeDatabaseItemsFlowUsecase,
        insertAnimeDatabaseItemUsecase = insertAnimeDatabaseItemUsecase,
        deleteAnimeDatabaseItemUsecase = deleteAnimeDatabaseItemUsecase,
        resetAllAnimeDatabaseItemsNewEpisodeStatusUsecase = resetAllAnimeDatabaseItemsNewEpisodeStatusUsecase,
        changeAnimeDatabaseItemNewEpisodeStatusUsecase = changeAnimeDatabaseItemNewEpisodeStatusUsecase,
        updateAnimeDatabaseItemUsecase = updateAnimeDatabaseItemUsecase
    )

    @Provides
    fun provideAnimeDatabaseExecutorFactory(
        coroutineContextProvider: CoroutineContextProvider,
        animeDatabaseUsecases: AnimeDatabaseUsecases
    ): () -> AnimeDatabaseExecutorImpl = {
        AnimeDatabaseExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = animeDatabaseUsecases
        )
    }

    @Provides
    fun provideAnimeDatabaseStore(
        storeFactory: StoreFactory,
        databaseExecutorFactory: () -> AnimeDatabaseExecutorImpl
    ): AnimeDatabaseStore = AnimeDatabaseStoreFactory(
        storeFactory = storeFactory,
        executorFactory = databaseExecutorFactory
    ).create()
}
