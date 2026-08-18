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

@Qualifier
internal annotation class ConnectionError

@Qualifier
internal annotation class UnknownError

/**
 * Provides the Android [CoroutineContextProvider] and [ToastProvider] bindings; mixed into
 * `:app`'s `DiAppComponent`. The `DateFormatter` binding is platform-independent and lives in
 * commonMain's `DiCelebrityComponent` instead.
 */
interface DiCelebrityPlatformComponent {
    @Provides
    @AppScope
    fun provideCoroutineContextProvider(
        @AppContext appContext: PlatformContext
    ): CoroutineContextProvider = CoroutineContextProviderPlatform(appContext = appContext)

    @Provides
    @AppScope
    @ConnectionError
    fun provideMakeConnectionErrorToast(
        @AppContext appContext: PlatformContext
    ): MakeConnectionErrorToast = { ToastManager.makeConnectionErrorToast(appContext) }

    @Provides
    @AppScope
    @UnknownError
    fun provideMakeUnknownErrorToast(
        @AppContext appContext: PlatformContext
    ): MakeUnknownErrorToast = { ToastManager.makeUnknownErrorToast(appContext) }

    @Provides
    @AppScope
    fun provideToastProvider(
        @ConnectionError makeConnectionErrorToast: MakeConnectionErrorToast,
        @UnknownError makeUnknownErrorToast: MakeUnknownErrorToast
    ): ToastProvider = ToastProvider(
        makeConnectionErrorToast = makeConnectionErrorToast,
        makeUnknownErrorToast = makeUnknownErrorToast
    )
}
