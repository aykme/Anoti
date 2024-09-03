package com.alekseivinogradov.anime_list.impl.presentation

import com.alekseivinogradov.anime_list.api.domain.store.section_content.SectionContentStore
import com.alekseivinogradov.anime_list.api.domain.store.upper_menu.UpperMenuStore
import com.alekseivinogradov.anime_list.api.presentation.AnimeListView
import com.alekseivinogradov.anime_list.api.presentation.mapper.store.mapUiEventToSectionContentIntent
import com.alekseivinogradov.anime_list.api.presentation.mapper.store.mapUiEventToUpperMenuIntent
import com.alekseivinogradov.anime_list.api.presentation.mapper.store.mapUpperMenuStateToOngoingSectionContentIntent
import com.alekseivinogradov.anime_list.impl.domain.store.section_content.SectionContentStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.store.upper_menu.UpperMenuStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.usecase.Usecases
import com.alekseivinogradov.anime_list.impl.presentation.mapper.model.mapStateToUiModel
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapNotNull

class AnimeListController(
    storeFactory: StoreFactory = DefaultStoreFactory(),
    lifecycle: Lifecycle,
    usecases: Usecases
) {

    private val upperMenuStore = UpperMenuStoreFactory(storeFactory).create()

    private val ongoingSectionContentStore = SectionContentStoreFactory(
        storeFactory = storeFactory,
        storeName = "OngoingSectionContentStore",
        fetchAnimeListUsecase = usecases.fetchAnimeOngoingListUsecase
    ).create()

    init {
        lifecycle.doOnDestroy { upperMenuStore.dispose() }
    }

    fun onViewCreated(mainView: AnimeListView, viewLifecycle: Lifecycle) {
        bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            mainView.events.mapNotNull(::mapUiEventToUpperMenuIntent) bindTo upperMenuStore

            mainView.events.mapNotNull(
                ::mapUiEventToSectionContentIntent
            ) bindTo ongoingSectionContentStore

            upperMenuStore.states.mapNotNull(
                ::mapUpperMenuStateToOngoingSectionContentIntent
            ) bindTo ongoingSectionContentStore

            subscribeOnAllRequiredStates() bindTo mainView
        }
    }

    private fun subscribeOnAllRequiredStates(): Flow<AnimeListView.UiModel> {
        return combine(
            upperMenuStore.states,
            ongoingSectionContentStore.states,
            ::stateToUiMapper
        )
    }

    private fun stateToUiMapper(
        upperMenuState: UpperMenuStore.State,
        ongoingSectionContentState: SectionContentStore.State
    ): AnimeListView.UiModel {
        return mapStateToUiModel(
            upperMenuState = upperMenuState,
            ongoingSectionContentState = ongoingSectionContentState
        )
    }
}