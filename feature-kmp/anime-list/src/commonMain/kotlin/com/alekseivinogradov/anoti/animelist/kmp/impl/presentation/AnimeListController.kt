package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store.mapAnnouncedStoreLabelToMainStoreIntent
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store.mapAnnouncedStoreStateToMainStoreIntent
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store.mapDatabaseStoreStateToMainStoreIntent
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store.mapMainStoreLabelToAnnouncedStoreIntent
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store.mapMainStoreLabelToDatabaseStoreIntent
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store.mapMainStoreLabelToOngoingStoreIntent
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store.mapMainStoreLabelToSearchStoreIntent
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store.mapOngoingStoreLabelToMainStoreIntent
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store.mapOngoingStoreStateToMainStoreIntent
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store.mapSearchStoreLabelToMainStoreIntent
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store.mapSearchStoreStateToMainStoreIntent
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.AnimeListView
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.mapper.model.mapStateToUiModel
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

/**
 * Wires the main store, its three section stores, and [AnimeDatabaseStore] to the view for the
 * screen's lifecycle.
 *
 * @param lifecycle screen lifecycle the store bindings are tied to.
 * @param mainStore the anime list screen's top-level store.
 * @param animeDatabaseStore saved-anime database store; drives notification state.
 * @param ongoingSectionStore the "ongoing" section's own store.
 * @param announcedSectionStore the "announced" section's own store.
 * @param searchSectionStore the search section's own store.
 */
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

    /**
     * Binds [mainView] to the main store for [viewLifecycle]'s duration.
     *
     * @param mainView view instance created for this lifecycle.
     * @param viewLifecycle the view's own lifecycle.
     */
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
