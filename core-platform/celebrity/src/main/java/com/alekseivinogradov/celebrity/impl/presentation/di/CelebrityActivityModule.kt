package com.alekseivinogradov.celebrity.impl.presentation.di

import android.content.Context
import com.alekseivinogradov.celebrity.api.domain.formatter.DateFormatter
import com.alekseivinogradov.celebrity.impl.presentation.formatter.DateFormatterImpl
import com.alekseivinogradov.di.api.presentation.ActivityContext
import com.alekseivinogradov.di.api.presentation.scope.ActivityScope
import dagger.Module
import dagger.Provides

@Module
interface CelebrityActivityModule {
    companion object {
        @Provides
        @ActivityScope
        fun provideDateFormatter(
            @ActivityContext activityContext: Context
        ): DateFormatter {
            return DateFormatterImpl(
                activityContext = activityContext,
                isAutomaticLanguageDetection = false
            )
        }
    }
}
