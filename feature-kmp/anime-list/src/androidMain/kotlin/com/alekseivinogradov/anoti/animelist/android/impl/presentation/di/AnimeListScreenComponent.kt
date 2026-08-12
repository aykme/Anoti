package com.alekseivinogradov.anoti.animelist.android.impl.presentation.di

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.arkivanov.decompose.ComponentContext

/**
 * Owns the anime-list screen's `FeatureScope` DI subgraph for as long as this component's
 * lifecycle (inherited from [componentContext]) is alive — created once when
 * `RootConfig.AnimeList` becomes the active root config, disposed when
 * `RootComponent.navigateTo()` replaces it. [AnimeListFragment] reads its dependencies from an
 * already-built instance of this class instead of creating its own `FeatureScope` graph.
 */
class AnimeListScreenComponent(
    componentContext: ComponentContext,
    animeListComponent: AnimeListComponent
) : ComponentContext by componentContext {

    val coroutineContextProvider: CoroutineContextProvider =
        animeListComponent.coroutineContextProvider
    val dateFormatter: DateFormatter = animeListComponent.dateFormatter
    val animeDatabaseStore: AnimeDatabaseStore = animeListComponent.animeDatabaseStore
    val mainStore: AnimeListMainStore = animeListComponent.mainStore
    val ongoingSectionStore: OngoingSectionStore = animeListComponent.ongoingSectionStore
    val announcedSectionStore: AnnouncedSectionStore = animeListComponent.announcedSectionStore
    val searchSectionStore: SearchSectionStore = animeListComponent.searchSectionStore
}
