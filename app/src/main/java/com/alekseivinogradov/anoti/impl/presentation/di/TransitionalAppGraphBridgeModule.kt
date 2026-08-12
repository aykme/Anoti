package com.alekseivinogradov.anoti.impl.presentation.di

import android.content.Context
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.FetchAllAnimeDatabaseItemsUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.UpdateAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.animenotification.android.impl.presentation.factory.AnimeNotificationChannelFactory
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

        @Provides
        @Singleton
        fun provideShikimoriApiService(
            graph: TransitionalAppGraph
        ): ShikimoriApiService = graph.shikimoriApiService

        /**
         * Not sourced from [TransitionalAppGraph]: [AnimeNotificationChannelFactory] is a
         * genuinely Android-only class (uses `android.app.NotificationChannel`), so it cannot be
         * declared as an `abstract val` on [TransitionalAppGraph] — that class is compiled for
         * iOS too from a single commonMain source set, and the type simply isn't visible there.
         * Its kotlin-inject `@Inject` constructor is invoked directly here instead, matching what
         * kotlin-inject's own generated factory would do.
         */
        @Provides
        @Singleton
        fun provideAnimeNotificationChannelFactory(
            coroutineContextProvider: CoroutineContextProvider
        ): AnimeNotificationChannelFactory =
            AnimeNotificationChannelFactory(coroutineContextProvider)
    }
}
