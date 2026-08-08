package com.alekseivinogradov.anoti.animedatabase.platform.impl.presentation.di.store

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.wrapper.AnimeDatabaseUsecases
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.AnimeDatabaseExecutorImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.AnimeDatabaseStoreFactory
import com.alekseivinogradov.anoti.animedatabase.platform.impl.presentation.di.store.usecase.AnimeDatabaseStoreUsecaseModule
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
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
