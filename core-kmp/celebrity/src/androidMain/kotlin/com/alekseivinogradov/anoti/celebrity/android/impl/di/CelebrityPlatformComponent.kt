package com.alekseivinogradov.anoti.celebrity.android.impl.di

import com.alekseivinogradov.anoti.celebrity.android.impl.domain.coroutinecontext.CoroutineContextProviderPlatform
import com.alekseivinogradov.anoti.celebrity.android.impl.presentation.toast.manager.ToastManager
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.MakeConnectionErrorToast
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.MakeUnknownErrorToast
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.di.kmp.PlatformContext
import com.alekseivinogradov.anoti.di.kmp.qualifier.AppContext
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Qualifier
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@Qualifier
internal annotation class ConnectionError

@Qualifier
internal annotation class UnknownError

/**
 * Contributes the Android [CoroutineContextProvider] and [ToastProvider] bindings to
 * [AppScope]'s merged component. The `DateFormatter` binding is platform-independent and lives
 * in commonMain's `CelebrityComponent` instead.
 */
@ContributesTo(AppScope::class)
interface CelebrityPlatformComponent {
    @Provides
    @SingleIn(AppScope::class)
    fun provideCoroutineContextProvider(
        @AppContext appContext: PlatformContext
    ): CoroutineContextProvider = CoroutineContextProviderPlatform(appContext = appContext)

    @Provides
    @SingleIn(AppScope::class)
    @ConnectionError
    fun provideMakeConnectionErrorToast(
        @AppContext appContext: PlatformContext
    ): MakeConnectionErrorToast = { ToastManager.makeConnectionErrorToast(appContext) }

    @Provides
    @SingleIn(AppScope::class)
    @UnknownError
    fun provideMakeUnknownErrorToast(
        @AppContext appContext: PlatformContext
    ): MakeUnknownErrorToast = { ToastManager.makeUnknownErrorToast(appContext) }

    @Provides
    @SingleIn(AppScope::class)
    fun provideToastProvider(
        @ConnectionError makeConnectionErrorToast: MakeConnectionErrorToast,
        @UnknownError makeUnknownErrorToast: MakeUnknownErrorToast
    ): ToastProvider = ToastProvider(
        makeConnectionErrorToast = makeConnectionErrorToast,
        makeUnknownErrorToast = makeUnknownErrorToast
    )
}
