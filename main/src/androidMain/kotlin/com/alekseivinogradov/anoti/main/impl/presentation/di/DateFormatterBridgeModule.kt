package com.alekseivinogradov.anoti.main.impl.presentation.di

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.di.kmp.TransitionalAppGraph
import dagger.Module
import dagger.Provides

@Module
internal interface DateFormatterBridgeModule {
    companion object {
        @Provides
        fun provideDateFormatter(graph: TransitionalAppGraph): DateFormatter = graph.dateFormatter
    }
}
