package com.alekseivinogradov.anoti.celebrity.ios.impl.di

import com.alekseivinogradov.anoti.celebrity.ios.impl.domain.coroutinecontext.CoroutineContextProviderPlatform
import com.alekseivinogradov.anoti.celebrity.ios.impl.presentation.toast.ToastManager
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * Contributes the iOS [CoroutineContextProvider] and [ToastProvider] bindings to [AppScope]'s
 * merged component. The `DateFormatter` binding is platform-independent and lives in
 * commonMain's `CelebrityComponent` instead.
 */
@ContributesTo(AppScope::class)
interface CelebrityPlatformComponent {
    @Provides
    @SingleIn(AppScope::class)
    fun provideCoroutineContextProvider(): CoroutineContextProvider = CoroutineContextProviderPlatform()

    @Provides
    @SingleIn(AppScope::class)
    fun provideToastProvider(): ToastProvider = ToastProvider(
        makeConnectionErrorToast = ToastManager.makeConnectionErrorToast(),
        makeUnknownErrorToast = ToastManager.makeUnknownErrorToast()
    )
}
