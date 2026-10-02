package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.AnimeFavoritesView
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.AnimeFavoritesUiModel
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.compose.AnimeFavoritesScreen
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.ComposeMviView
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.lifecycle.ChildLifecycle

/**
 * Renders the anime-favorites screen of [screenComponent]. Each composition gets its own view,
 * bound to the component's controller for as long as the composition lasts. The first one opens
 * the section.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
fun AnimeFavoritesRoute(screenComponent: NavAnimeFavoritesScreenComponent) {
    // Needs an explicit AnimeFavoritesView supertype: the controller takes that interface, and
    // ComposeMviView's structural match to it isn't enough for Kotlin's nominal typing.
    val composeView = remember(screenComponent) {
        object :
            ComposeMviView<AnimeFavoritesUiModel, AnimeFavoritesMainStore.Intent>(),
            AnimeFavoritesView {}
    }
    // An effect, not "remember": a discarded composition still runs "remember", and its binder
    // would never be stopped.
    DisposableEffect(screenComponent) {
        val viewLifecycle = ChildLifecycle(parent = screenComponent.lifecycle)
        screenComponent.controller.onViewCreated(
            mainView = composeView,
            viewLifecycle = viewLifecycle
        )
        screenComponent.openSectionUnlessRestored()
        onDispose { viewLifecycle.destroy() }
    }
    composeView.model.value?.let { uiModel ->
        AnimeFavoritesScreen(
            uiModel = uiModel,
            dateFormatter = screenComponent.dateFormatter,
            dispatch = composeView::dispatch
        )
    }
}
