package com.alekseivinogradov.anime_database.room.impl.presentation.di.store

import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anime_database.api.domain.usecase.wrapper.AnimeDatabaseUsecases
import com.alekseivinogradov.anime_database.impl.domain.store.AnimeDatabaseExecutorImpl
import com.alekseivinogradov.anime_database.impl.domain.store.AnimeDatabaseStoreFactory
import com.alekseivinogradov.anime_database.room.impl.presentation.di.store.usecase.AnimeDatabaseStoreUsecaseModule
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dagger.Module
import dagger.Provides

@Module(includes = [AnimeDatabaseStoreUsecaseModule::class])
interface AnimeDatabaseStoreModule {
    companion object {
        @Provides
        fun provideAnimeDatabaseExecutorFactory(
            coroutineContextProvider: CoroutineContextProvider,
            animeDatabaseUsecases: AnimeDatabaseUsecases
        ): () -> AnimeDatabaseExecutorImpl {
            return {
                AnimeDatabaseExecutorImpl(
                    coroutineContextProvider = coroutineContextProvider,
                    usecases = animeDatabaseUsecases
                )
            }
        }

        @Provides
        fun provideAnimeDatabaseStore(
            storeFactory: StoreFactory,
            databaseExecutorFactory: () -> AnimeDatabaseExecutorImpl
        ): AnimeDatabaseStore {
            return AnimeDatabaseStoreFactory(
                storeFactory = storeFactory,
                executorFactory = databaseExecutorFactory
            ).create()
        }
    }
}
