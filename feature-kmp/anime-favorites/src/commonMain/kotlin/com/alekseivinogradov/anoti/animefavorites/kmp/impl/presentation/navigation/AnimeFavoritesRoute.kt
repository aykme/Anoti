package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.AnimeFavoritesView
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.UiModel
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.AnimeFavoritesController
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.compose.AnimeFavoritesScreen
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.ComposeMviView

/**
 * Renders the anime-favorites screen for as long as [screenComponent] stays the active root
 * child — a new [screenComponent] instance (after navigating away and back) gets its own fresh
 * view and controller, matching the store's own per-activation lifetime.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
fun AnimeFavoritesRoute(screenComponent: NavAnimeFavoritesScreenComponent) {
    // Needs an explicit AnimeFavoritesView supertype: the controller takes that interface, and
    // ComposeMviView's structural match to it isn't enough for Kotlin's nominal typing.
    val composeView = remember(screenComponent) {
        object : ComposeMviView<UiModel, AnimeFavoritesMainStore.Intent>(), AnimeFavoritesView {}
    }
    // Binding runs as an effect, not inside "remember": a discarded/retried
    // composition still executes "remember" calculator, which would start a second,
    // uncanceled MVIKotlin binder alongside the one from the composition that actually commits.
    LaunchedEffect(screenComponent) {
        AnimeFavoritesController(
            lifecycle = screenComponent.lifecycle,
            mainStore = screenComponent.mainStore,
            animeDatabaseStore = screenComponent.animeDatabaseStore
        ).onViewCreated(mainView = composeView, viewLifecycle = screenComponent.lifecycle)
    }
    composeView.model.value?.let { uiModel ->
        AnimeFavoritesScreen(
            uiModel = uiModel,
            dateFormatter = screenComponent.dateFormatter,
            dispatch = composeView::dispatch
        )
    }
}
