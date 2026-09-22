package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.navigation

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SearchDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionHatDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.di.DiAnimeListComponent
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
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

    /** Coroutine contexts the screen's executors run on. */
    val coroutineContextProvider: CoroutineContextProvider =
        diAnimeListComponent.coroutineContextProvider

    /** Formats the air dates the screen shows. */
    val dateFormatter: DateFormatter = diAnimeListComponent.dateFormatter

    /** The app-wide saved-anime store; drives the items' notification state. */
    val animeDatabaseStore: AnimeDatabaseStore = diAnimeListComponent.animeDatabaseStore

    /** The screen's top-level store. */
    val mainStore: AnimeListMainStore = diAnimeListComponent.mainStore

    /** The "ongoing" section's own store. */
    val ongoingSectionStore: OngoingSectionStore = diAnimeListComponent.ongoingSectionStore

    /** The "announced" section's own store. */
    val announcedSectionStore: AnnouncedSectionStore = diAnimeListComponent.announcedSectionStore

    /** The search section's own store. */
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
                searchType = state.search.type,
                searchText = state.search.searchText,
                ongoing = ongoingSectionStore.state.sectionContent.let {
                    RestoredSectionState(
                        itemCount = it.listItems.size,
                        enabledExtraEpisodesInfoIds = it.enabledExtraEpisodesInfoIds,
                        nextEpisodesInfo = it.animeDetails.nextEpisodesInfo
                    )
                },
                announced = announcedSectionStore.state.sectionContent.let {
                    RestoredSectionState(
                        itemCount = it.listItems.size,
                        enabledExtraEpisodesInfoIds = it.enabledExtraEpisodesInfoIds
                    )
                },
                search = searchSectionStore.state.sectionContent.let {
                    RestoredSectionState(
                        itemCount = it.listItems.size,
                        enabledExtraEpisodesInfoIds = it.enabledExtraEpisodesInfoIds,
                        nextEpisodesInfo = it.animeDetails.nextEpisodesInfo
                    )
                }
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
    val searchType: SearchDomain.Type,
    val searchText: String,
    val ongoing: RestoredSectionState,
    val announced: RestoredSectionState,
    val search: RestoredSectionState
)

/**
 * A section's saved snapshot: how many items it had loaded, and its display state.
 *
 * @param itemCount how many items were loaded before restore.
 * @param enabledExtraEpisodesInfoIds ids that were showing the extra episode-info variant.
 * @param nextEpisodesInfo next-episode air date/time by anime id, already fetched before restore.
 * Always empty for the announced section, which never fetches this.
 */
@Serializable
internal data class RestoredSectionState(
    val itemCount: Int,
    val enabledExtraEpisodesInfoIds: Set<AnimeId>,
    val nextEpisodesInfo: Map<AnimeId, String?> = mapOf()
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
 * Each section's display state ([RestoredSectionState]) is replayed into that section's own
 * store regardless of which section ends up selected. Applying it is a pure state update with no
 * network call, so there's no cost to doing it for a section the user hasn't switched back to
 * yet. And [OngoingSectionStore.Intent.OpenSection] and its Announced/Search equivalents already
 * use each store's own [RestoredSectionState] to page in the right number of items once that
 * section actually opens, whether that's right now or from a later manual tap.
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
    if (restoredState != null) {
        ongoingSectionStore.accept(
            OngoingSectionStore.Intent.RestoreSection(
                itemCount = restoredState.ongoing.itemCount,
                enabledExtraEpisodesInfoIds = restoredState.ongoing.enabledExtraEpisodesInfoIds,
                nextEpisodesInfo = restoredState.ongoing.nextEpisodesInfo
            )
        )
        announcedSectionStore.accept(
            AnnouncedSectionStore.Intent.RestoreSection(
                itemCount = restoredState.announced.itemCount,
                enabledExtraEpisodesInfoIds = restoredState.announced.enabledExtraEpisodesInfoIds
            )
        )
        searchSectionStore.accept(
            SearchSectionStore.Intent.RestoreSection(
                itemCount = restoredState.search.itemCount,
                enabledExtraEpisodesInfoIds = restoredState.search.enabledExtraEpisodesInfoIds,
                nextEpisodesInfo = restoredState.search.nextEpisodesInfo
            )
        )
    }

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
        // SearchSectionClick above always leaves the search bar SHOWN. The bar can be hidden
        // independently of the selected section (the user can cancel it without leaving the
        // section), so a restored HIDDEN state must be reapplied on top of that default.
        if (restoredState?.searchType == SearchDomain.Type.HIDDEN) {
            mainStore.accept(AnimeListMainStore.Intent.CancelSearchClick)
        }
    }
}
