package com.alekseivinogradov.anoti.impl.presentation.di

import android.content.Context
import com.alekseivinogradov.anoti.celebrity.android.api.presentation.di.AppContext
import com.alekseivinogradov.anoti.di.kmp.TransitionalAppGraph
import com.alekseivinogradov.anoti.di.kmp.create
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
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
    }
}
