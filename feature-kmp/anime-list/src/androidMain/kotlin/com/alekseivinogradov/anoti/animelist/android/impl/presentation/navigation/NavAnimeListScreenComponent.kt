package com.alekseivinogradov.anoti.animelist.android.impl.presentation.navigation

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.di.DiAnimeListComponent
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy

/**
 * Owns the anime-list screen's `FeatureScope` DI subgraph for as long as this component's
 * lifecycle (inherited from [componentContext]) is alive — created once when
 * `NavRootConfig.AnimeList` becomes the active root config, disposed when
 * `NavRootComponent.navigateTo()` replaces it.
 * [com.alekseivinogradov.anoti.animelist.android.impl.presentation.AnimeListFragment]
 * reads its dependencies from an
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

    init {
        // Registered here rather than in AnimeListController so the stores are still disposed
        // when this component is replaced before any Fragment ever builds its controller.
        lifecycle.doOnDestroy {
            ongoingSectionStore.dispose()
            announcedSectionStore.dispose()
            searchSectionStore.dispose()
            animeDatabaseStore.dispose()
            mainStore.dispose()
        }
    }
}
