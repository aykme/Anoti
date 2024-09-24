package com.alekseivinogradov.anime_list.impl.presentation

import com.alekseivinogradov.anime_list.api.domain.store.mapper.mapAnnouncedStoreStateToMainStoreIntent
import com.alekseivinogradov.anime_list.api.data.local.mapper.store.mapDatabaseStoreStateToMainStoreIntent
import com.alekseivinogradov.anime_list.api.domain.store.mapper.mapMainStoreLabelToAnnouncedStoreIntent
import com.alekseivinogradov.anime_list.api.data.local.mapper.store.mapMainStoreLabelToDatabaseStoreIntent
import com.alekseivinogradov.anime_list.api.domain.store.mapper.mapMainStoreLabelToOngoingStoreIntent
import com.alekseivinogradov.anime_list.api.domain.store.mapper.mapMainStoreLabelToSearchStoreIntent
import com.alekseivinogradov.anime_list.api.domain.store.mapper.mapOngoingStoreStateToMainStoreIntent
import com.alekseivinogradov.anime_list.api.domain.store.mapper.mapSearchStoreStateToMainStoreIntent
import com.alekseivinogradov.anime_list.api.presentation.AnimeListView
import com.alekseivinogradov.anime_list.api.presentation.mapper.model.mapStateToUiModel
import com.alekseivinogradov.anime_list.impl.domain.store.announced_section.AnnouncedSectionStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.store.main.AnimeListMainStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.store.ongoing_section.OngoingSectionStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.store.search_section.SearchSectionStoreFactory
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.AnnouncedUsecases
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.OngoingUsecases
import com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper.SearchUsecases
import com.alekseivinogradov.database.impl.domain.store.DatabaseStoreFactory
import com.alekseivinogradov.database.impl.domain.usecase.DatabaseUsecases
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class AnimeListController(
    storeFactory: StoreFactory = DefaultStoreFactory(),
    lifecycle: Lifecycle,
    databaseUsecases: DatabaseUsecases,
    ongoingUsecases: OngoingUsecases,
    announcedUsecases: AnnouncedUsecases,
    searchUsecases: SearchUsecases
) {

    private val mainStore = AnimeListMainStoreFactory(
        storeFactory = storeFactory
    ).create()

    private val databaseStore = DatabaseStoreFactory(
        storeFactory = storeFactory,
        databaseUsecases = databaseUsecases
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
            mainView.events bindTo mainStore
            mainStore.states.map(::mapStateToUiModel) bindTo mainView
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
        }
    }
}