package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.navigation

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.di.DiAnimeFavoritesComponent
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.AnimeFavoritesController
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.serialization.Serializable

/**
 * Owns the anime-favorites screen's `FeatureScope` DI subgraph for as long as this component's
 * lifecycle (inherited from [componentContext]) is alive — created once when
 * `NavRootConfig.AnimeFavorites` becomes the active root config, disposed when
 * `NavRootComponent.navigateTo()` replaces it. It builds the screen's [controller];
 * [AnimeFavoritesRoute] only binds a view per composition and opens the section.
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

    // stateKeeper only round-trips a value through the platform's saved state on genuine
    // process recreation, never on an ordinary top-level navigation switch (this component is
    // fully destroyed and recreated then, before ever having registered anything). So a non-null
    // consume() here means process death happened while this screen was the active one.
    private val wasRestoredFromProcessDeath: Boolean =
        stateKeeper.consume(key = RESTORED_MARKER_KEY, strategy = RestoredMarker.serializer()) !=
            null

    init {
        stateKeeper.register(key = RESTORED_MARKER_KEY, strategy = RestoredMarker.serializer()) {
            RestoredMarker
        }

        lifecycle.doOnDestroy {
            animeDatabaseStore.dispose()
            mainStore.dispose()
        }
    }

    /** Wires the screen's stores while this component lives, and a view per composition. */
    val controller = AnimeFavoritesController(
        lifecycle = lifecycle,
        mainStore = mainStore,
        animeDatabaseStore = animeDatabaseStore
    )

    /**
     * Opens the section once per component. Opening drives [mainStore]'s minimum-visible loading
     * state, so every arrival gets the same loading treatment. The extra-info reset is skipped
     * after process death while the section was open, so that display state survives. Every other
     * arrival resets it.
     *
     * A later composition of the same component finds the content type moved off its untouched
     * default and does nothing.
     *
     * Resets [animeDatabaseStore] directly rather than through [mainStore]'s
     * `Label.ResetExtraInfo`: that label only reaches [animeDatabaseStore] once the controller's
     * binder has started, which is posted to a later main-thread turn.
     */
    fun openSectionUnlessRestored() {
        if (mainStore.state.contentType != ContentTypeDomain.LOADING()) return
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
