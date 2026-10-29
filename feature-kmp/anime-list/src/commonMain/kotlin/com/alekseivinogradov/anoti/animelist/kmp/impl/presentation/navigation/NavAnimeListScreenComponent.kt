package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.navigation

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SearchDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionHatDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.di.DiAnimeListComponent
import com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.AnimeListController
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.infodialog.kmp.api.domain.store.InfoDialogStore
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import kotlinx.serialization.Serializable

/**
 * Owns the anime-list screen's `FeatureScope` DI subgraph for as long as this component's
 * lifecycle (inherited from [componentContext]) is alive — created once when
 * `NavRootConfig.AnimeList` becomes the active root config, disposed when
 * `NavRootComponent.navigateTo()` replaces it. It builds the screen's [controller], holds the
 * information dialog's store beside the screen's own, and replays the saved state on its first
 * start; [AnimeListRoute] only draws the controller's state.
 */
class NavAnimeListScreenComponent(
    componentContext: ComponentContext,
    diAnimeListComponent: DiAnimeListComponent
) : ComponentContext by componentContext {

    /** Formats the air dates the screen shows. */
    val dateFormatter: DateFormatter = diAnimeListComponent.dateFormatter

    /** The app-wide saved-anime store; drives the items' notification state. */
    internal val animeDatabaseStore: AnimeDatabaseStore = diAnimeListComponent.animeDatabaseStore

    /** The screen's top-level store. */
    internal val mainStore: AnimeListMainStore = diAnimeListComponent.mainStore

    /** The "ongoing" section's own store. */
    internal val ongoingSectionStore: OngoingSectionStore = diAnimeListComponent.ongoingSectionStore

    /** The "announced" section's own store. */
    internal val announcedSectionStore: AnnouncedSectionStore =
        diAnimeListComponent.announcedSectionStore

    /** The search section's own store. */
    internal val searchSectionStore: SearchSectionStore = diAnimeListComponent.searchSectionStore

    /** The information dialog's own store. */
    internal val infoDialogStore: InfoDialogStore = diAnimeListComponent.infoDialogStore

    init {
        // Registered before the saved state is read. A state the screen rejects throws there, and
        // the stores must still close with the lifecycle.
        lifecycle.doOnDestroy {
            ongoingSectionStore.dispose()
            announcedSectionStore.dispose()
            searchSectionStore.dispose()
            infoDialogStore.dispose()
            animeDatabaseStore.dispose()
            mainStore.dispose()
        }
    }

    // Consumed once at construction, per StateKeeper's contract, and replayed when the component
    // first starts.
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
                        enabledExtraEpisodesInfoIds = it.enabledExtraEpisodesInfoIds
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
                        enabledExtraEpisodesInfoIds = it.enabledExtraEpisodesInfoIds
                    )
                },
                isInfoDialogShown = infoDialogStore.state.isShown
            )
        }

        // Subscribed before the controller binds, so the replay always comes before the wiring.
        // Under an immediate dispatcher the wiring would otherwise open the search section before
        // its restored text arrives.
        lifecycle.doOnStart(isOneTime = true) {
            applyRestoredMainState(
                restoredState = restoredState,
                mainStore = mainStore,
                ongoingSectionStore = ongoingSectionStore,
                announcedSectionStore = announcedSectionStore,
                searchSectionStore = searchSectionStore,
                infoDialogStore = infoDialogStore
            )
        }
    }

    /** Wires the screen's stores while this component lives and hands the UI their state. */
    val controller = AnimeListController(
        lifecycle = lifecycle,
        mainStore = mainStore,
        animeDatabaseStore = animeDatabaseStore,
        ongoingSectionStore = ongoingSectionStore,
        announcedSectionStore = announcedSectionStore,
        searchSectionStore = searchSectionStore,
        infoDialogStore = infoDialogStore
    )

    private companion object {
        private const val RESTORED_STATE_KEY = "AnimeListMainStoreRestoredState"
    }
}

/**
 * The screen's saved snapshot, replayed by [applyRestoredMainState] on the first start.
 *
 * @param selectedSection the section the user was on.
 * @param searchType whether the search bar was shown.
 * @param searchText what the search field held.
 * @param ongoing the "ongoing" section's snapshot.
 * @param announced the "announced" section's snapshot.
 * @param search the search section's snapshot.
 * @param isInfoDialogShown whether the information dialog was open. Its default keeps a state
 * saved without this field decodable.
 */
@Serializable
internal data class RestoredMainState(
    val selectedSection: SectionHatDomain,
    val searchType: SearchDomain.Type,
    val searchText: String,
    val ongoing: RestoredSectionState,
    val announced: RestoredSectionState,
    val search: RestoredSectionState,
    val isInfoDialogShown: Boolean = false
)

/**
 * A section's saved snapshot: how many items it had loaded, and its display state.
 *
 * @param itemCount how many items were loaded before restore.
 * @param enabledExtraEpisodesInfoIds ids that were showing the extra episode-info variant.
 */
@Serializable
internal data class RestoredSectionState(
    val itemCount: Int,
    val enabledExtraEpisodesInfoIds: Set<AnimeId>
)

/**
 * Dispatches straight to the section stores rather than through their own
 * `OpenAnnouncedSection`/`OpenSearchSection` labels. It runs before `AnimeListController`'s
 * wiring starts, so a label sent now would reach no store. `mainStore`'s own
 * selected-section/search-text UI state is still updated through its normal click intents,
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
 * Reopens the information dialog when it was open, straight into its store.
 *
 * @param restoredState the saved snapshot, or `null` on a fresh (non-restored) start.
 */
// One parameter per store the replay reaches directly.
@Suppress("LongParameterList")
internal fun applyRestoredMainState(
    restoredState: RestoredMainState?,
    mainStore: AnimeListMainStore,
    ongoingSectionStore: OngoingSectionStore,
    announcedSectionStore: AnnouncedSectionStore,
    searchSectionStore: SearchSectionStore,
    infoDialogStore: InfoDialogStore
) {
    if (restoredState != null) {
        ongoingSectionStore.accept(
            OngoingSectionStore.Intent.RestoreSection(
                itemCount = restoredState.ongoing.itemCount,
                enabledExtraEpisodesInfoIds = restoredState.ongoing.enabledExtraEpisodesInfoIds
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
                enabledExtraEpisodesInfoIds = restoredState.search.enabledExtraEpisodesInfoIds
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
    // Empty, not blank: the field restores whatever was typed, spaces included, and a query the
    // stores never heard about would leave the two showing different things.
    val restoredSearchText = restoredState?.searchText
    if (!restoredSearchText.isNullOrEmpty()) {
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

    if (restoredState?.isInfoDialogShown == true) {
        infoDialogStore.accept(InfoDialogStore.Intent.InfoClick)
    }
}
