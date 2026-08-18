package com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.navigation

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.di.AnimeFavoritesComponent
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy

/**
 * Owns the anime-favorites screen's `FeatureScope` DI subgraph for as long as this component's
 * lifecycle (inherited from [componentContext]) is alive — created once when
 * `NavRootConfig.AnimeFavorites` becomes the active root config, disposed when
 * `NavRootComponent.navigateTo()` replaces it.
 * [com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.AnimeFavoritesFragment]
 * reads its dependencies from
 * an already-built instance of this class instead of creating its own `FeatureScope` graph.
 */
class NavAnimeFavoritesScreenComponent(
    componentContext: ComponentContext,
    animeFavoritesComponent: AnimeFavoritesComponent
) : ComponentContext by componentContext {

    val coroutineContextProvider: CoroutineContextProvider =
        animeFavoritesComponent.coroutineContextProvider
    val dateFormatter: DateFormatter = animeFavoritesComponent.dateFormatter
    val animeDatabaseStore: AnimeDatabaseStore = animeFavoritesComponent.animeDatabaseStore
    val mainStore: AnimeFavoritesMainStore = animeFavoritesComponent.mainStore

    init {
        // Registered here rather than in AnimeFavoritesController so the stores are still
        // disposed when this component is replaced before any Fragment ever builds its
        // controller.
        lifecycle.doOnDestroy {
            animeDatabaseStore.dispose()
            mainStore.dispose()
        }
    }
}
