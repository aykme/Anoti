package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.navigation

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.di.DiAnimeFavoritesComponent
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.AnimeFavoritesController
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import kotlinx.serialization.Serializable

/**
 * Owns the anime-favorites screen's `FeatureScope` DI subgraph for as long as this component's
 * lifecycle (inherited from [componentContext]) is alive — created once when
 * `NavRootConfig.AnimeFavorites` becomes the active root config, disposed when
 * `NavRootComponent.navigateTo()` replaces it. It builds the screen's [controller] and opens the
 * section on its first start; [AnimeFavoritesRoute] only draws the controller's state.
 */
class NavAnimeFavoritesScreenComponent(
    componentContext: ComponentContext,
    diAnimeFavoritesComponent: DiAnimeFavoritesComponent
) : ComponentContext by componentContext {

    /** Coroutine contexts the screen's executor runs on. */
    val coroutineContextProvider: CoroutineContextProvider =
        diAnimeFavoritesComponent.coroutineContextProvider

    /** Formats the air dates the screen shows. */
    val dateFormatter: DateFormatter = diAnimeFavoritesComponent.dateFormatter

    /** The app-wide saved-anime store; the source of the favorites list. */
    val animeDatabaseStore: AnimeDatabaseStore = diAnimeFavoritesComponent.animeDatabaseStore

    /** The screen's own store. */
    val mainStore: AnimeFavoritesMainStore = diAnimeFavoritesComponent.mainStore

    init {
        // Registered before the saved state is read. A state the screen rejects throws there, and
        // the stores must still close with the lifecycle.
        lifecycle.doOnDestroy {
            animeDatabaseStore.dispose()
            mainStore.dispose()
        }
    }

    // stateKeeper hands a value back only when the screen is rebuilt from saved state: after
    // process death, or after a platform rebuild of its host. A top-level navigation switch
    // creates this component afresh, with nothing registered. So a non-null consume() means the
    // screen was rebuilt while it was the active one.
    private val wasRestoredFromSavedState: Boolean =
        stateKeeper.consume(key = RESTORED_MARKER_KEY, strategy = RestoredMarker.serializer()) !=
            null

    init {
        stateKeeper.register(key = RESTORED_MARKER_KEY, strategy = RestoredMarker.serializer()) {
            RestoredMarker
        }

        // Subscribed before the controller binds, so the section opens before the database's
        // first list reaches the store. Opening waits for a list that arrives after it.
        lifecycle.doOnStart(isOneTime = true) { openSection() }
    }

    /** Wires the screen's stores while this component lives and hands the UI their state. */
    val controller = AnimeFavoritesController(
        lifecycle = lifecycle,
        mainStore = mainStore,
        animeDatabaseStore = animeDatabaseStore
    )

    /**
     * Opens the section when the component first starts. Opening drives [mainStore]'s
     * minimum-visible loading state, so every arrival gets the same loading treatment. The
     * extra-info reset is skipped when the screen comes back from saved state, so that display
     * state survives. Every other arrival resets it.
     *
     * Resets [animeDatabaseStore] directly rather than through [mainStore]'s
     * `Label.ResetExtraInfo`: that label only reaches [animeDatabaseStore] once the controller's
     * binder has started, which is posted to a later main-thread turn.
     */
    private fun openSection() {
        mainStore.accept(AnimeFavoritesMainStore.Intent.OpenSection)
        if (!wasRestoredFromSavedState) {
            animeDatabaseStore.accept(AnimeDatabaseStore.Intent.ResetAllItemsExtraInfo)
        }
    }

    private companion object {
        private const val RESTORED_MARKER_KEY = "AnimeFavoritesRestoredMarker"
    }
}

@Serializable
internal data object RestoredMarker
