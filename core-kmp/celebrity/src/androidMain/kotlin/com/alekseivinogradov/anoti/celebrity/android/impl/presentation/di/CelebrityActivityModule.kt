package com.alekseivinogradov.anoti.celebrity.android.impl.presentation.di

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.formatter.DateFormatterImpl
import com.alekseivinogradov.anoti.di.android.api.presentation.scope.ActivityScope
import dagger.Module
import dagger.Provides

@Module
interface CelebrityActivityModule {
    companion object {
        @Provides
        @ActivityScope
        fun provideDateFormatter(): DateFormatter {
            return DateFormatterImpl()
        }
    }
}
