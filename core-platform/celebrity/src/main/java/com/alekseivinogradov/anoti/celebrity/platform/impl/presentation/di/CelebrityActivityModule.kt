package com.alekseivinogradov.anoti.celebrity.platform.impl.presentation.di

import android.content.Context
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.platform.impl.presentation.formatter.DateFormatterImpl
import com.alekseivinogradov.anoti.di.platform.api.presentation.ActivityContext
import com.alekseivinogradov.anoti.di.platform.api.presentation.scope.ActivityScope
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
