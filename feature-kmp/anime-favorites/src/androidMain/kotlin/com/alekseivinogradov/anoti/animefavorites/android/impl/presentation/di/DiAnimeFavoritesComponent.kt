package com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.di

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.di.kmp.scope.ActivityScope
import com.alekseivinogradov.anoti.di.kmp.scope.FeatureScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesSubcomponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * The anime-favorites screen's [FeatureScope] graph — one instance per `AnimeFavoritesFragment`,
 * created from the hosting Activity's [ActivityScope] graph through [Factory]. Merges this
 * module's commonMain `DiAnimeFavoritesComponent` contributions (the source, its usecases and the
 * main store).
 */
@ContributesSubcomponent(FeatureScope::class)
@SingleIn(FeatureScope::class)
interface DiAnimeFavoritesComponent {
    /** The app-wide [CoroutineContextProvider], inherited from the app-scope graph. */
    val coroutineContextProvider: CoroutineContextProvider

    /** The [DateFormatter], inherited from the app-scope graph. */
    val dateFormatter: DateFormatter

    /** The app-wide [AnimeDatabaseStore], inherited from the app-scope graph. */
    val animeDatabaseStore: AnimeDatabaseStore

    /** The screen's [AnimeFavoritesMainStore], see commonMain's `DiAnimeFavoritesComponent`. */
    val mainStore: AnimeFavoritesMainStore

    /**
     * Builds [DiAnimeFavoritesComponent]s; contributed to the hosting [ActivityScope] graph. The
     * function name has to be unique across every subcomponent factory merged into that graph —
     * they all end up on one generated interface, where same-named functions returning different
     * component types would clash.
     */
    @ContributesSubcomponent.Factory(ActivityScope::class)
    interface Factory {
        fun createDiAnimeFavoritesComponent(): DiAnimeFavoritesComponent
    }
}
