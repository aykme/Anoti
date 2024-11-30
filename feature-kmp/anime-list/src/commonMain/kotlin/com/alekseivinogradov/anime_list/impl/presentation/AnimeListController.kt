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
import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore
import com.alekseivinogradov.anime_list.api.presentation.AnimeListView
import com.alekseivinogradov.anime_list.api.presentation.mapper.model.mapStateToUiModel
import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class AnimeListController(
    lifecycle: Lifecycle,
    private val mainStore: AnimeListMainStore,
    private val animeDatabaseStore: AnimeDatabaseStore,
    private val ongoingSectionStore: OngoingSectionStore,
    private val announcedSectionStore: AnnouncedSectionStore,
    private val searchSectionStore: SearchSectionStore
) {

    init {
        lifecycle.doOnDestroy { ongoingSectionStore.dispose() }
        lifecycle.doOnDestroy { announcedSectionStore.dispose() }
        lifecycle.doOnDestroy { searchSectionStore.dispose() }
        lifecycle.doOnDestroy { animeDatabaseStore.dispose() }
        lifecycle.doOnDestroy { mainStore.dispose() }
    }

    fun onViewCreated(mainView: AnimeListView, viewLifecycle: Lifecycle) {
        connectAllAuxiliaryStoresToMain(viewLifecycle)
        connectMainStoreToMainView(mainView = mainView, viewLifecycle = viewLifecycle)
    }

    private fun connectAllAuxiliaryStoresToMain(viewLifecycle: Lifecycle) {
        bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            animeDatabaseStore.states.map(
                ::mapDatabaseStoreStateToMainStoreIntent
            ) bindTo mainStore

            mainStore.labels.mapNotNull(
                ::mapMainStoreLabelToDatabaseStoreIntent
            ) bindTo animeDatabaseStore

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

    private fun connectMainStoreToMainView(
        mainView: AnimeListView,
        viewLifecycle: Lifecycle
    ) {
        bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            mainStore.states.map(::mapStateToUiModel) bindTo mainView
            mainView.events bindTo mainStore
        }
    }
}
