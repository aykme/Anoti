package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.navigation

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.di.DiAnimeFavoritesComponent
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.serialization.Serializable

/**
 * Owns the anime-favorites screen's `FeatureScope` DI subgraph for as long as this component's
 * lifecycle (inherited from [componentContext]) is alive — created once when
 * `NavRootConfig.AnimeFavorites` becomes the active root config, disposed when
 * `NavRootComponent.navigateTo()` replaces it. [AnimeFavoritesRoute] reads its dependencies from
 * an already-built instance of this class instead of creating its own `FeatureScope` graph.
 */
class NavAnimeFavoritesScreenComponent(
    componentContext: ComponentContext,
    diAnimeFavoritesComponent: DiAnimeFavoritesComponent
) : ComponentContext by componentContext {

    val coroutineContextProvider: CoroutineContextProvider =
        diAnimeFavoritesComponent.coroutineContextProvider
    val dateFormatter: DateFormatter = diAnimeFavoritesComponent.dateFormatter
    val animeDatabaseStore: AnimeDatabaseStore = diAnimeFavoritesComponent.animeDatabaseStore
    val mainStore: AnimeFavoritesMainStore = diAnimeFavoritesComponent.mainStore

    // stateKeeper only round-trips a value through a real Android Bundle on genuine process
    // recreation, never on an ordinary top-level navigation switch (this component is fully
    // destroyed and recreated then, before ever having registered anything). So a non-null
    // consume() here means process death happened while this screen was the active one.
    private val wasRestoredFromProcessDeath: Boolean =
        stateKeeper.consume(key = RESTORED_MARKER_KEY, strategy = RestoredMarker.serializer()) !=
            null

    init {
        stateKeeper.register(key = RESTORED_MARKER_KEY, strategy = RestoredMarker.serializer()) {
            RestoredMarker
        }

        // Registered here rather than in AnimeFavoritesController so the stores are still
        // disposed when this component is replaced before AnimeFavoritesRoute ever builds its
        // controller.
        lifecycle.doOnDestroy {
            animeDatabaseStore.dispose()
            mainStore.dispose()
        }
    }

    /**
     * Opens the section unless process death happened while it was already open — in that one
     * case its extra-info display state should survive, not reset. Every other way of arriving
     * here (bottom-nav switch, deep link) always resets.
     */
    fun openSectionUnlessRestored() {
        if (!wasRestoredFromProcessDeath) {
            mainStore.accept(AnimeFavoritesMainStore.Intent.OpenSection)
        }
    }

    private companion object {
        private const val RESTORED_MARKER_KEY = "AnimeFavoritesRestoredMarker"
    }
}

@Serializable
internal data object RestoredMarker
