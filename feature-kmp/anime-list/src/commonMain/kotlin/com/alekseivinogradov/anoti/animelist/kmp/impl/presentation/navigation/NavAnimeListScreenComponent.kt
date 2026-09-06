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
     * Opens whichever section should be active: replayed from a snapshot saved before process
     * death, or the default section on a fresh start. Runs once per instance. See
     * [applyRestoredMainState] for why this dispatches directly to the section stores.
     */
    fun applyRestoredStateIfAny() {
        applyRestoredMainState(
            restoredState = restoredState,
            mainStore = mainStore,
            ongoingSectionStore = ongoingSectionStore,
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
 * Dispatches straight to the section stores rather than through their own
 * `OpenAnnouncedSection`/`OpenSearchSection` labels. Those labels are only delivered once
 * `AnimeListController`'s binder has started collecting the publishing store's `labels`. That
 * collector attaches via `BuilderBinder.start()`, which launches through
 * `GlobalScope.launch(mainContext)` — a real, asynchronous dispatch. It isn't guaranteed to have
 * happened yet by the time this runs, so a label published before it attaches is silently
 * dropped. Dispatching directly to the target store has no such ordering requirement. `mainStore`'s
 * own selected-section/search-text UI state is still updated through its normal click intents,
 * synchronously.
 *
 * Opens exactly one section: whichever was restored, or [SectionHatDomain.ONGOINGS] as the
 * default on a fresh, non-restored start. No section bootstraps its own content anymore, so
 * skipping this call would leave every section stuck loading forever.
 *
 * Never forces [AnimeListMainStore.Intent.ChangeResetListPositionFlag]. The list's scroll offset
 * is restored independently by Compose's own saved-state mechanism. Forcing a reset here would
 * discard it.
 *
 * The search text is replayed independently of which section ends up selected: a query typed
 * while on the search section survives switching to another section, so it must still reach
 * [searchSectionStore] even when a different section is the one being restored as selected.
 *
 * @param restoredState the saved snapshot, or `null` on a fresh (non-restored) start.
 */
internal fun applyRestoredMainState(
    restoredState: RestoredMainState?,
    mainStore: AnimeListMainStore,
    ongoingSectionStore: OngoingSectionStore,
    announcedSectionStore: AnnouncedSectionStore,
    searchSectionStore: SearchSectionStore
) {
    val selectedSection = restoredState?.selectedSection ?: SectionHatDomain.ONGOINGS
    when (selectedSection) {
        SectionHatDomain.ONGOINGS -> {
            ongoingSectionStore.accept(OngoingSectionStore.Intent.OpenSection)
        }

        SectionHatDomain.ANNOUNCED -> {
            mainStore.accept(AnimeListMainStore.Intent.AnnouncedSectionClick)
            announcedSectionStore.accept(AnnouncedSectionStore.Intent.OpenSection)
        }

        SectionHatDomain.SEARCH -> mainStore.accept(AnimeListMainStore.Intent.SearchSectionClick)
    }

    // Applied before OpenSection: SearchSectionStore seeds its debounced search flow from the
    // current search text the moment OpenSection runs. Restoring the text first means a later
    // OpenSection (here or from a subsequent manual tap into the section) already fetches the
    // restored query instead of blank results.
    val restoredSearchText = restoredState?.searchText
    if (!restoredSearchText.isNullOrBlank()) {
        mainStore.accept(AnimeListMainStore.Intent.ChangeSearchText(restoredSearchText))
        searchSectionStore.accept(
            SearchSectionStore.Intent.ChangeSearchText(restoredSearchText)
        )
    }

    if (selectedSection == SectionHatDomain.SEARCH) {
        searchSectionStore.accept(SearchSectionStore.Intent.OpenSection)
    }
}
