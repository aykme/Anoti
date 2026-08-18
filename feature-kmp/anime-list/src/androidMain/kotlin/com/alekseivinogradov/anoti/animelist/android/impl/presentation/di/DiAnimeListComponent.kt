package com.alekseivinogradov.anoti.animelist.android.impl.presentation.di

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.di.kmp.scope.ActivityScope
import com.alekseivinogradov.anoti.di.kmp.scope.FeatureScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesSubcomponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * The anime-list screen's [FeatureScope] graph — one instance per `AnimeListFragment`, created
 * from the hosting Activity's [ActivityScope] graph through [Factory]. Merges this module's
 * commonMain `DiAnimeListComponent` contributions (the source, its usecases and the four stores).
 */
@ContributesSubcomponent(FeatureScope::class)
@SingleIn(FeatureScope::class)
interface DiAnimeListComponent {
    /** The app-wide [CoroutineContextProvider], inherited from the app-scope graph. */
    val coroutineContextProvider: CoroutineContextProvider

    /** The [DateFormatter], inherited from the app-scope graph. */
    val dateFormatter: DateFormatter

    /** The app-wide [AnimeDatabaseStore], inherited from the app-scope graph. */
    val animeDatabaseStore: AnimeDatabaseStore

    /** The screen's [AnimeListMainStore], see commonMain's `DiAnimeListComponent`. */
    val mainStore: AnimeListMainStore

    /** The screen's [OngoingSectionStore], see commonMain's `DiAnimeListComponent`. */
    val ongoingSectionStore: OngoingSectionStore

    /** The screen's [AnnouncedSectionStore], see commonMain's `DiAnimeListComponent`. */
    val announcedSectionStore: AnnouncedSectionStore

    /** The screen's [SearchSectionStore], see commonMain's `DiAnimeListComponent`. */
    val searchSectionStore: SearchSectionStore

    /**
     * Builds [DiAnimeListComponent]s; contributed to the hosting [ActivityScope] graph. The
     * function name has to be unique across every subcomponent factory merged into that graph —
     * they all end up on one generated interface, where same-named functions returning different
     * component types would clash.
     */
    @ContributesSubcomponent.Factory(ActivityScope::class)
    interface Factory {
        fun createDiAnimeListComponent(): DiAnimeListComponent
    }
}
