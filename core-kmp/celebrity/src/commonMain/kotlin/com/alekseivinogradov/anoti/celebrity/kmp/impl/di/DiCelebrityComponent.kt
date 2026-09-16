package com.alekseivinogradov.anoti.celebrity.kmp.impl.di

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.controller.ToastController
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.connection_error
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.unknown_error
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderDefaultImpl
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.formatter.DateFormatterImpl
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import me.tatarka.inject.annotations.Provides

/**
 * Provides this module's app-wide bindings; mixed into `DiAppComponent` on both platforms.
 */
interface DiCelebrityComponent {
    @Provides
    @AppScope
    fun provideStoreFactory(): StoreFactory = DefaultStoreFactory()

    // Deliberately unscoped: DateFormatterImpl is stateless, so every injection point gets its
    // own instance instead of sharing one for the app's lifetime.
    @Provides
    fun provideDateFormatter(): DateFormatter = DateFormatterImpl()

    @Provides
    @AppScope
    fun provideToastController(): ToastController = ToastController()

    @Provides
    @AppScope
    fun provideCoroutineContextProvider(
        toastController: ToastController
    ): CoroutineContextProvider = CoroutineContextProviderDefaultImpl(toastController)

    @Provides
    @AppScope
    fun provideToastProvider(toastController: ToastController): ToastProvider = ToastProvider(
        makeConnectionErrorToast = { toastController.show(Res.string.connection_error) },
        makeUnknownErrorToast = { toastController.show(Res.string.unknown_error) }
    )
}
