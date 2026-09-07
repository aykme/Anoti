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
     * Opens the section, always — this drives [mainStore]'s minimum-visible-duration loading
     * state, so every arrival gets the same non-flickery loading treatment. Only the extra-info
     * reset is conditional: skipped when process death happened while the section was already
     * open, so that display state survives instead of resetting. Every other way of arriving
     * here (bottom-nav switch, deep link) always resets it.
     *
     * Resets [animeDatabaseStore] directly rather than through [mainStore]'s
     * `Label.ResetExtraInfo`: that label only reaches [animeDatabaseStore] once
     * `AnimeFavoritesController`'s binder has attached, which — called this early, right after
     * the controller is constructed — isn't guaranteed yet.
     */
    fun openSectionUnlessRestored() {
        mainStore.accept(AnimeFavoritesMainStore.Intent.OpenSection)
        if (!wasRestoredFromProcessDeath) {
            animeDatabaseStore.accept(AnimeDatabaseStore.Intent.ResetAllItemsExtraInfo)
        }
    }

    private companion object {
        private const val RESTORED_MARKER_KEY = "AnimeFavoritesRestoredMarker"
    }
}

@Serializable
internal data object RestoredMarker
