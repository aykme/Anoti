package com.alekseivinogradov.anoti.celebrity.ios.impl.di

import com.alekseivinogradov.anoti.celebrity.ios.impl.domain.coroutinecontext.CoroutineContextProviderPlatform
import com.alekseivinogradov.anoti.celebrity.ios.impl.presentation.toast.ToastManager
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import me.tatarka.inject.annotations.Provides

/**
 * Contributes the iOS [CoroutineContextProvider] and [ToastProvider] bindings to [AppScope]'s
 * merged component. The `DateFormatter` binding is platform-independent and lives in
 * commonMain's `DiCelebrityComponent` instead.
 */
interface DiCelebrityPlatformComponent {
    @Provides
    @AppScope
    fun provideCoroutineContextProvider(): CoroutineContextProvider = CoroutineContextProviderPlatform()

    @Provides
    @AppScope
    fun provideToastProvider(): ToastProvider = ToastProvider(
        makeConnectionErrorToast = ToastManager.makeConnectionErrorToast(),
        makeUnknownErrorToast = ToastManager.makeUnknownErrorToast()
    )
}
