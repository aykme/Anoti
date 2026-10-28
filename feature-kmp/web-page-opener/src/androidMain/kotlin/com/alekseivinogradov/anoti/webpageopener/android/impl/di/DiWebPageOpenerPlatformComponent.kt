package com.alekseivinogradov.anoti.webpageopener.android.impl.di

import com.alekseivinogradov.anoti.di.kmp.PlatformContext
import com.alekseivinogradov.anoti.di.kmp.qualifier.AppContext
import com.alekseivinogradov.anoti.webpageopener.android.impl.domain.usecase.OpenWebPageUsecaseImpl
import com.alekseivinogradov.anoti.webpageopener.kmp.api.domain.usecase.OpenWebPageUsecase
import me.tatarka.inject.annotations.Provides

/**
 * Binds the Android [OpenWebPageUsecase]; mixed into `core-kmp:di-app`'s Android
 * `DiAppComponent`.
 */
interface DiWebPageOpenerPlatformComponent {
    @Provides
    fun provideOpenWebPageUsecase(@AppContext appContext: PlatformContext): OpenWebPageUsecase =
        OpenWebPageUsecaseImpl(appContext = appContext)
}
