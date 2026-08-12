package com.alekseivinogradov.anoti.celebrity.ios.impl.di

import com.alekseivinogradov.anoti.celebrity.ios.impl.domain.coroutinecontext.CoroutineContextProviderPlatform
import com.alekseivinogradov.anoti.celebrity.ios.impl.presentation.toast.ToastManager
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.MakeConnectionErrorToast
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.MakeUnknownErrorToast
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.formatter.DateFormatterImpl
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * Contributes the iOS [CoroutineContextProvider], [ToastProvider] and (temporarily, see
 * Phase 3 note in the migration plan) [DateFormatter] bindings to [AppScope]'s merged component.
 */
@ContributesTo(AppScope::class)
interface CelebrityPlatformComponent {
    @Provides
    @SingleIn(AppScope::class)
    fun provideCoroutineContextProvider(): CoroutineContextProvider = CoroutineContextProviderPlatform()

    @Provides
    @SingleIn(AppScope::class)
    fun provideToastProvider(): ToastProvider = ToastProvider(
        makeConnectionErrorToast = ToastManager.makeConnectionErrorToast() as MakeConnectionErrorToast,
        makeUnknownErrorToast = ToastManager.makeUnknownErrorToast() as MakeUnknownErrorToast
    )

    @Provides
    // temporarily AppScope, see Phase 3 note — restored to ActivityScope in Phase 9
    fun provideDateFormatter(): DateFormatter = DateFormatterImpl()
}
