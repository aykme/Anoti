package com.alekseivinogradov.anoti.infodialog.kmp.impl.di

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import com.alekseivinogradov.anoti.infodialog.kmp.api.domain.store.InfoDialogStore
import com.alekseivinogradov.anoti.infodialog.kmp.impl.domain.store.InfoDialogExecutorFactory
import com.alekseivinogradov.anoti.infodialog.kmp.impl.domain.store.InfoDialogExecutorImpl
import com.alekseivinogradov.anoti.infodialog.kmp.impl.domain.store.InfoDialogStoreFactory
import com.alekseivinogradov.anoti.infodialog.kmp.impl.domain.usecase.wrapper.InfoDialogUsecases
import com.alekseivinogradov.anoti.webpageopener.kmp.api.domain.usecase.OpenWebPageUsecase
import com.arkivanov.mvikotlin.core.store.StoreFactory
import me.tatarka.inject.annotations.Provides

/**
 * The information dialog's bindings. Mixed into the feature graph of each screen that shows the
 * dialog, which supplies the store factory, the coroutine contexts, the system messages and the
 * web page usecase.
 */
interface DiInfoDialogComponent {
    @Provides
    fun provideInfoDialogUsecases(openWebPageUsecase: OpenWebPageUsecase): InfoDialogUsecases =
        InfoDialogUsecases(openWebPageUsecase = openWebPageUsecase)

    @Provides
    fun provideInfoDialogExecutorFactory(
        coroutineContextProvider: CoroutineContextProvider,
        usecases: InfoDialogUsecases,
        systemMessageProvider: SystemMessageProvider
    ): InfoDialogExecutorFactory = {
        InfoDialogExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = usecases,
            systemMessageProvider = systemMessageProvider
        )
    }

    @Provides
    fun provideInfoDialogStore(
        storeFactory: StoreFactory,
        executorFactory: InfoDialogExecutorFactory
    ): InfoDialogStore = InfoDialogStoreFactory(
        storeFactory = storeFactory,
        executorFactory = executorFactory
    ).create()
}
