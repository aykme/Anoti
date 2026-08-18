package com.alekseivinogradov.anoti.main.impl.di

import com.alekseivinogradov.anoti.animefavorites.kmp.impl.di.DiAnimeFavoritesComponent
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.di.DiAnimeFavoritesDependencies
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.di.createDiAnimeFavoritesComponent
import com.alekseivinogradov.anoti.animelist.kmp.impl.di.DiAnimeListComponent
import com.alekseivinogradov.anoti.animelist.kmp.impl.di.DiAnimeListDependencies
import com.alekseivinogradov.anoti.animelist.kmp.impl.di.createDiAnimeListComponent
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.di.DiBottomNavigationBarComponent
import com.alekseivinogradov.anoti.di.kmp.scope.RootScope
import me.tatarka.inject.annotations.Component

/**
 * The root UI host's graph — one instance per host, built from the app-wide component. Owns the
 * bottom navigation bar store and builds the per-screen components.
 */
@Component
@RootScope
abstract class DiRootComponent(
    @Component val parent: DiRootDependencies
) : DiBottomNavigationBarComponent,
    DiAnimeListDependencies,
    DiAnimeFavoritesDependencies {

    /** The host's [BottomNavigationBarStore]. */
    abstract val bottomNavigationBarStore: BottomNavigationBarStore

    /** Builds the anime-list screen's component. */
    fun createDiAnimeListComponent(): DiAnimeListComponent =
        createDiAnimeListComponent(parent = this)

    /** Builds the anime-favorites screen's component. */
    fun createDiAnimeFavoritesComponent(): DiAnimeFavoritesComponent =
        createDiAnimeFavoritesComponent(parent = this)
}
