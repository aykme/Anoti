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
    // applyRestoredStateIfAny() once the section stores are actually wired to mainStore.
    private val restoredState: RestoredMainState? =
        stateKeeper.consume(key = RESTORED_STATE_KEY, strategy = RestoredMainState.serializer())

    init {
        stateKeeper.register(key = RESTORED_STATE_KEY, strategy = RestoredMainState.serializer()) {
            val state = mainStore.state
            RestoredMainState(
                selectedSection = state.selectedSection.name,
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
     * Must run after [AnimeListController] has wired the section stores to [mainStore] — before
     * that, the labels this dispatches (`OpenAnnouncedSection`/`OpenSearchSection`) would have no
     * subscriber and would be silently lost. The ongoing section needs no replay: it's the
     * default selection, and [OngoingSectionStore] already bootstraps its own content on
     * creation regardless of this restore.
     */
    fun applyRestoredStateIfAny() {
        val restoredState = restoredState ?: return
        when (SectionHatDomain.valueOf(restoredState.selectedSection)) {
            SectionHatDomain.ANNOUNCED -> {
                mainStore.accept(AnimeListMainStore.Intent.AnnouncedSectionClick)
            }

            SectionHatDomain.SEARCH -> {
                mainStore.accept(AnimeListMainStore.Intent.SearchSectionClick)
                if (restoredState.searchText.isNotBlank()) {
                    mainStore.accept(
                        AnimeListMainStore.Intent.ChangeSearchText(restoredState.searchText)
                    )
                }
            }

            SectionHatDomain.ONGOINGS -> Unit
        }
    }

    private companion object {
        private const val RESTORED_STATE_KEY = "AnimeListMainStoreRestoredState"
    }
}

@Serializable
private data class RestoredMainState(
    val selectedSection: String,
    val searchText: String
)
