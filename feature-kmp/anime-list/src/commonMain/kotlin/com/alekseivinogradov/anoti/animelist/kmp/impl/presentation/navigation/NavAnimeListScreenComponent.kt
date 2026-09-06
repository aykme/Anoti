package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.navigation

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionHatDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.di.DiAnimeListComponent
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.serialization.Serializable

/**
 * Owns the anime-list screen's `FeatureScope` DI subgraph for as long as this component's
 * lifecycle (inherited from [componentContext]) is alive — created once when
 * `NavRootConfig.AnimeList` becomes the active root config, disposed when
 * `NavRootComponent.navigateTo()` replaces it. [AnimeListRoute] reads its dependencies from an
 * already-built instance of this class instead of creating its own `FeatureScope` graph.
 */
class NavAnimeListScreenComponent(
    componentContext: ComponentContext,
    diAnimeListComponent: DiAnimeListComponent
) : ComponentContext by componentContext {

    val coroutineContextProvider: CoroutineContextProvider =
        diAnimeListComponent.coroutineContextProvider
    val dateFormatter: DateFormatter = diAnimeListComponent.dateFormatter
    val animeDatabaseStore: AnimeDatabaseStore = diAnimeListComponent.animeDatabaseStore
    val mainStore: AnimeListMainStore = diAnimeListComponent.mainStore
    val ongoingSectionStore: OngoingSectionStore = diAnimeListComponent.ongoingSectionStore
    val announcedSectionStore: AnnouncedSectionStore = diAnimeListComponent.announcedSectionStore
    val searchSectionStore: SearchSectionStore = diAnimeListComponent.searchSectionStore

    // Consumed once here (construction time), per StateKeeper's contract; replayed later via
    // applyRestoredStateIfAny(), once the section stores exist to dispatch to directly.
    private val restoredState: RestoredMainState? =
        stateKeeper.consume(key = RESTORED_STATE_KEY, strategy = RestoredMainState.serializer())

    init {
        stateKeeper.register(key = RESTORED_STATE_KEY, strategy = RestoredMainState.serializer()) {
            val state = mainStore.state
            RestoredMainState(
                selectedSection = state.selectedSection,
                searchText = state.search.searchText
            )
        }

        // Registered here rather than in AnimeListController so the stores are still disposed
        // when this component is replaced before AnimeListRoute ever builds its controller.
        lifecycle.doOnDestroy {
            ongoingSectionStore.dispose()
            announcedSectionStore.dispose()
            searchSectionStore.dispose()
            animeDatabaseStore.dispose()
            mainStore.dispose()
        }
    }

    /**
     * Replays the section/search selection saved before process death, once per instance.
     * See [applyRestoredMainState] for why this dispatches directly to the section stores.
     */
    fun applyRestoredStateIfAny() {
        applyRestoredMainState(
            restoredState = restoredState,
            mainStore = mainStore,
            announcedSectionStore = announcedSectionStore,
            searchSectionStore = searchSectionStore
        )
    }

    private companion object {
        private const val RESTORED_STATE_KEY = "AnimeListMainStoreRestoredState"
    }
}

@Serializable
internal data class RestoredMainState(
    val selectedSection: SectionHatDomain,
    val searchText: String
)

/**
 * Dispatches straight to [announcedSectionStore]/[searchSectionStore] rather than through the
 * section stores' own `OpenAnnouncedSection`/`OpenSearchSection` labels: those labels are only
 * delivered once `AnimeListController`'s binder has started collecting the publishing store's
 * `labels`, which (`BuilderBinder.start()`) launches via `GlobalScope.launch(mainContext)` — a
 * real, asynchronous dispatch, not something guaranteed to have happened by the time this runs. A
 * label published before that collector attaches is silently dropped. Dispatching directly to the
 * target store has no such ordering requirement. `mainStore`'s own selected-section/search-text UI
 * state is still updated through its normal click intents, synchronously. The ongoing section
 * needs no replay: it's the default selection, and [OngoingSectionStore] already bootstraps its
 * own content on creation regardless of this restore.
 *
 * Never forces [AnimeListMainStore.Intent.ChangeResetListPositionFlag]: the list's scroll offset
 * is restored independently by Compose's own saved-state mechanism, and forcing a reset here would
 * discard it.
 *
 * @param restoredState the saved snapshot, or `null` on a fresh (non-restored) start.
 */
internal fun applyRestoredMainState(
    restoredState: RestoredMainState?,
    mainStore: AnimeListMainStore,
    announcedSectionStore: AnnouncedSectionStore,
    searchSectionStore: SearchSectionStore
) {
    if (restoredState == null) return
    when (restoredState.selectedSection) {
        SectionHatDomain.ANNOUNCED -> {
            mainStore.accept(AnimeListMainStore.Intent.AnnouncedSectionClick)
            announcedSectionStore.accept(AnnouncedSectionStore.Intent.OpenSection)
        }

        SectionHatDomain.SEARCH -> {
            mainStore.accept(AnimeListMainStore.Intent.SearchSectionClick)
            // Search text must be applied before OpenSection: SearchSectionStore seeds its
            // debounced search flow from the current search text the moment OpenSection runs, so
            // restoring the text first means that first load already fetches the restored query
            // instead of blank results.
            if (restoredState.searchText.isNotBlank()) {
                mainStore.accept(
                    AnimeListMainStore.Intent.ChangeSearchText(restoredState.searchText)
                )
                searchSectionStore.accept(
                    SearchSectionStore.Intent.ChangeSearchText(restoredState.searchText)
                )
            }
            searchSectionStore.accept(SearchSectionStore.Intent.OpenSection)
        }

        SectionHatDomain.ONGOINGS -> Unit
    }
}
