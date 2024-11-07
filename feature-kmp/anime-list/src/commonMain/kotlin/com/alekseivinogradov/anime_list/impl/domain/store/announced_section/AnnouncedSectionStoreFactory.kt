package com.alekseivinogradov.anime_list.impl.domain.store.announced_section

import com.alekseivinogradov.anime_base.api.domain.ToastProvider
import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionExecutor
import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.AnnouncedUsecases
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

class AnnouncedSectionStoreFactory(
    private val storeFactory: StoreFactory,
    coroutineContextProvider: CoroutineContextProvider,
    usecases: AnnouncedUsecases,
    toastProvider: ToastProvider
) {
    private val executorFactory: () -> AnnouncedSectionExecutor = {
        AnnouncedSectionExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = usecases,
            toastProvider = toastProvider
        )
    }

    fun create(): AnnouncedSectionStore {
        return object : AnnouncedSectionStore,
            Store<
                    AnnouncedSectionStore.Intent,
                    AnnouncedSectionStore.State,
                    AnnouncedSectionStore.Label
                    >
            by storeFactory.create(
                name = "AnnouncedSectionStore",
                initialState = AnnouncedSectionStore.State(),
                bootstrapper = SimpleBootstrapper(),
                executorFactory = executorFactory,
                reducer = AnnouncedSectionReducerImpl()
            ) {}
    }
}
