package com.alekseivinogradov.anime_list.impl.presentation

import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.upper_menu.UpperMenuStore
import com.alekseivinogradov.anime_list.api.presentation.AnimeListView
import com.alekseivinogradov.anime_list.api.presentation.mapper.model.UiModel
import com.alekseivinogradov.anime_list.api.presentation.mapper.store.mapUiEventToAnnouncedSectionIntent
import com.alekseivinogradov.anime_list.api.presentation.mapper.store.mapUiEventToOngoingSectionIntent
import com.alekseivinogradov.anime_list.api.presentation.mapper.store.mapUiEventToSearchSectionIntent
import com.alekseivinogradov.anime_list.api.presentation.mapper.store.mapUiEventToUpperMenuIntent
import com.alekseivinogradov.anime_list.api.presentation.mapper.store.mapUpperMenuStateToAnnouncedSectionIntent
import com.alekseivinogradov.anime_list.api.presentation.mapper.store.mapUpperMenuStateToOngoingSectionIntent
import com.alekseivinogradov.anime_list.api.presentation.mapper.store.mapUpperMenuStateToSearchSectionIntent
import com.alekseivinogradov.anime_list.impl.domain.store.announced_section.AnnouncedSectionStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.store.ongoing_section.OngoingSectionStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.store.search_section.SearchSectionStoreFactory
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

    private val ongoingSectionStore = OngoingSectionStoreFactory(
        storeFactory = storeFactory,
        fetchAnimeListUsecase = usecases.fetchAnimeOngoingListUsecase
    ).create()

    private val announcedSectionStore = AnnouncedSectionStoreFactory(
        storeFactory = storeFactory,
        fetchAnimeListUsecase = usecases.fetchAnimeAnnouncedListUsecase
    ).create()

    private val searchSectionStore = SearchSectionStoreFactory(
        storeFactory = storeFactory,
        fetchAnimeListUsecase = usecases.fetchAnimeListBySearchUsecase
    ).create()

    init {
        lifecycle.doOnDestroy { upperMenuStore.dispose() }
    }

    fun onViewCreated(mainView: AnimeListView, viewLifecycle: Lifecycle) {
        bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            mainView.events.mapNotNull(::mapUiEventToUpperMenuIntent) bindTo upperMenuStore

            mainView.events.mapNotNull(
                ::mapUiEventToOngoingSectionIntent
            ) bindTo ongoingSectionStore

            mainView.events.mapNotNull(
                ::mapUiEventToAnnouncedSectionIntent
            ) bindTo announcedSectionStore

            mainView.events.mapNotNull(
                ::mapUiEventToSearchSectionIntent
            ) bindTo searchSectionStore

            upperMenuStore.states.mapNotNull(
                ::mapUpperMenuStateToOngoingSectionIntent
            ) bindTo ongoingSectionStore

            upperMenuStore.states.mapNotNull(
                ::mapUpperMenuStateToAnnouncedSectionIntent
            ) bindTo announcedSectionStore

            upperMenuStore.states.mapNotNull(
                ::mapUpperMenuStateToSearchSectionIntent
            ) bindTo searchSectionStore

            subscribeOnAllRequiredStates() bindTo mainView
        }
    }

    private fun subscribeOnAllRequiredStates(): Flow<UiModel> {
        return combine(
            upperMenuStore.states,
            ongoingSectionStore.states,
            announcedSectionStore.states,
            searchSectionStore.states,
            ::stateToUiMapper
        )
    }

    private fun stateToUiMapper(
        upperMenuState: UpperMenuStore.State,
        ongoingSectionState: OngoingSectionStore.State,
        announcedSectiontState: AnnouncedSectionStore.State,
        searchSectionState: SearchSectionStore.State,
    ): UiModel {
        return mapStateToUiModel(
            upperMenuState = upperMenuState,
            ongoingSectionState = ongoingSectionState,
            announcedSectionState = announcedSectiontState,
            searchSectionState = searchSectionState
        )
    }
}