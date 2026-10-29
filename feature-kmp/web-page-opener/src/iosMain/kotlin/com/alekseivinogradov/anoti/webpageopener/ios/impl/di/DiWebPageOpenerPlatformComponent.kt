package com.alekseivinogradov.anoti.webpageopener.ios.impl.di

import com.alekseivinogradov.anoti.webpageopener.ios.impl.domain.usecase.OpenWebPageUsecaseImpl
import com.alekseivinogradov.anoti.webpageopener.kmp.api.domain.usecase.OpenWebPageUsecase
import me.tatarka.inject.annotations.Provides
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * Binds the iOS [OpenWebPageUsecase] to UIKit; mixed into `core-kmp:di-app`'s iOS
 * `DiAppComponent`.
 */
interface DiWebPageOpenerPlatformComponent {
    @Provides
    fun provideOpenWebPageUsecase(): OpenWebPageUsecase = OpenWebPageUsecaseImpl(
        openUrl = { url: NSURL, onResult: (Boolean) -> Unit ->
            UIApplication.sharedApplication.openURL(
                url = url,
                options = emptyMap<Any?, Any?>(),
                completionHandler = onResult
            )
        }
    )
}
