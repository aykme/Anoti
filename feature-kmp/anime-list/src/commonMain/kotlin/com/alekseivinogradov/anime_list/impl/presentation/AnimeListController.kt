package com.alekseivinogradov.anime_list.impl.presentation

import com.alekseivinogradov.anime_list.api.domain.mapper.store.mapAnnouncedStoreLabelToMainStoreIntent
import com.alekseivinogradov.anime_list.api.domain.mapper.store.mapAnnouncedStoreStateToMainStoreIntent
import com.alekseivinogradov.anime_list.api.domain.mapper.store.mapDatabaseStoreStateToMainStoreIntent
import com.alekseivinogradov.anime_list.api.domain.mapper.store.mapMainStoreLabelToAnnouncedStoreIntent
import com.alekseivinogradov.anime_list.api.domain.mapper.store.mapMainStoreLabelToDatabaseStoreIntent
import com.alekseivinogradov.anime_list.api.domain.mapper.store.mapMainStoreLabelToOngoingStoreIntent
import com.alekseivinogradov.anime_list.api.domain.mapper.store.mapMainStoreLabelToSearchStoreIntent
import com.alekseivinogradov.anime_list.api.domain.mapper.store.mapOngoingStoreLabelToMainStoreIntent
import com.alekseivinogradov.anime_list.api.domain.mapper.store.mapOngoingStoreStateToMainStoreIntent
import com.alekseivinogradov.anime_list.api.domain.mapper.store.mapSearchStoreLabelToMainStoreIntent
import com.alekseivinogradov.anime_list.api.domain.mapper.store.mapSearchStoreStateToMainStoreIntent
import com.alekseivinogradov.anime_list.api.presentation.AnimeListView
import com.alekseivinogradov.anime_list.api.presentation.mapper.model.mapStateToUiModel
import com.alekseivinogradov.anime_list.impl.domain.store.announced_section.AnnouncedSectionStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.store.main.AnimeListMainStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.store.ongoing_section.OngoingSectionStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.store.search_section.SearchSectionStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.AnnouncedUsecases
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.OngoingUsecases
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.SearchUsecases
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.impl.domain.store.DatabaseStoreFactory
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class AnimeListController(
    storeFactory: StoreFactory,
    lifecycle: Lifecycle,
    databaseRepository: AnimeDatabaseRepository,
    ongoingUsecases: OngoingUsecases,
    announcedUsecases: AnnouncedUsecases,
    searchUsecases: SearchUsecases
) {

    private val mainStore = AnimeListMainStoreFactory(
        storeFactory = storeFactory
    ).create()

    private val databaseStore = DatabaseStoreFactory(
        storeFactory = storeFactory,
        repository = databaseRepository
    ).create()

    private val ongoingSectionStore = OngoingSectionStoreFactory(
        storeFactory = storeFactory,
        ongoingUsecases = ongoingUsecases
    ).create()

    private val announcedSectionStore = AnnouncedSectionStoreFactory(
        storeFactory = storeFactory,
        announcedUsecases = announcedUsecases
    ).create()

    private val searchSectionStore = SearchSectionStoreFactory(
        storeFactory = storeFactory,
        searchUsecases = searchUsecases
    ).create()

    init {
        lifecycle.doOnDestroy { mainStore.dispose() }
        lifecycle.doOnDestroy { databaseStore.dispose() }
        lifecycle.doOnDestroy { ongoingSectionStore.dispose() }
        lifecycle.doOnDestroy { announcedSectionStore.dispose() }
        lifecycle.doOnDestroy { searchSectionStore.dispose() }
    }

    fun onViewCreated(mainView: AnimeListView, viewLifecycle: Lifecycle) {
        bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            connectAllRequiredStores(viewLifecycle)
            mainStore.states.map(::mapStateToUiModel) bindTo mainView
            mainView.events bindTo mainStore
        }
    }

    private fun connectAllRequiredStores(viewLifecycle: Lifecycle) {
        bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            databaseStore.states.map(
                ::mapDatabaseStoreStateToMainStoreIntent
            ) bindTo mainStore

            mainStore.labels.mapNotNull(
                ::mapMainStoreLabelToDatabaseStoreIntent
            ) bindTo databaseStore

            ongoingSectionStore.states.map(
                ::mapOngoingStoreStateToMainStoreIntent
            ) bindTo mainStore

            announcedSectionStore.states.map(
                ::mapAnnouncedStoreStateToMainStoreIntent
            ) bindTo mainStore

            searchSectionStore.states.map(
                ::mapSearchStoreStateToMainStoreIntent
            ) bindTo mainStore

            mainStore.labels.mapNotNull(
                ::mapMainStoreLabelToOngoingStoreIntent
            ) bindTo ongoingSectionStore

            mainStore.labels.mapNotNull(
                ::mapMainStoreLabelToAnnouncedStoreIntent
            ) bindTo announcedSectionStore

            mainStore.labels.mapNotNull(
                ::mapMainStoreLabelToSearchStoreIntent
            ) bindTo searchSectionStore

            ongoingSectionStore.labels.map(
                ::mapOngoingStoreLabelToMainStoreIntent
            ) bindTo mainStore

            announcedSectionStore.labels.map(
                ::mapAnnouncedStoreLabelToMainStoreIntent
            ) bindTo mainStore

            searchSectionStore.labels.map(
                ::mapSearchStoreLabelToMainStoreIntent
            ) bindTo mainStore
        }
    }
}
