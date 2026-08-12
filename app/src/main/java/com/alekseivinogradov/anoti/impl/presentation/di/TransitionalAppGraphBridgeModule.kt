package com.alekseivinogradov.anoti.impl.presentation.di

import android.content.Context
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.FetchAllAnimeDatabaseItemsUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.UpdateAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.celebrity.android.api.presentation.di.AppContext
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.di.kmp.TransitionalAppGraph
import com.alekseivinogradov.anoti.di.kmp.create
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dagger.Module
import dagger.Provides
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
internal interface TransitionalAppGraphBridgeModule {
    companion object {
        @Provides
        @Singleton
        fun provideTransitionalAppGraph(
            @AppContext appContext: Context
        ): TransitionalAppGraph = TransitionalAppGraph::class.create(appContext)

        @Provides
        @Singleton
        fun provideHttpClient(graph: TransitionalAppGraph): HttpClient = graph.httpClient

        @Provides
        @Singleton
        fun provideSafeApi(graph: TransitionalAppGraph): SafeApi = graph.safeApi

        @Provides
        @Singleton
        fun provideStoreFactory(graph: TransitionalAppGraph): StoreFactory = graph.storeFactory

        @Provides
        @Singleton
        fun provideCoroutineContextProvider(
            graph: TransitionalAppGraph
        ): CoroutineContextProvider = graph.coroutineContextProvider

        @Provides
        @Singleton
        fun provideToastProvider(graph: TransitionalAppGraph): ToastProvider = graph.toastProvider

        @Provides
        @Singleton
        fun provideDateFormatter(graph: TransitionalAppGraph): DateFormatter = graph.dateFormatter

        @Provides
        @Singleton
        fun provideAnimeDatabaseStore(
            graph: TransitionalAppGraph
        ): AnimeDatabaseStore = graph.animeDatabaseStore

        @Provides
        @Singleton
        fun provideFetchAllAnimeDatabaseItemsUsecase(
            graph: TransitionalAppGraph
        ): FetchAllAnimeDatabaseItemsUsecase = graph.fetchAllAnimeDatabaseItemsUsecase

        @Provides
        @Singleton
        fun provideUpdateAnimeDatabaseItemUsecase(
            graph: TransitionalAppGraph
        ): UpdateAnimeDatabaseItemUsecase = graph.updateAnimeDatabaseItemUsecase
    }
}
