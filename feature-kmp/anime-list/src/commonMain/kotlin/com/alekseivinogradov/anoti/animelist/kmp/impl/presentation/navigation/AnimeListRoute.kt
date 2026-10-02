package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.AnimeListView
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.AnimeListUiModel
import com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.compose.AnimeListScreen
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.ComposeMviView
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.lifecycle.ChildLifecycle

/**
 * Renders the anime-list screen of [screenComponent]. Each composition gets its own view, bound
 * to the component's controller for as long as the composition lasts.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
fun AnimeListRoute(screenComponent: NavAnimeListScreenComponent) {
    // Needs an explicit AnimeListView supertype: the controller takes that interface, and
    // ComposeMviView's structural match to it isn't enough for Kotlin's nominal typing.
    val composeView = remember(screenComponent) {
        object : ComposeMviView<AnimeListUiModel, AnimeListMainStore.Intent>(), AnimeListView {}
    }
    // An effect, not "remember": a discarded composition still runs "remember", and its binder
    // would never be stopped.
    DisposableEffect(screenComponent) {
        val viewLifecycle = ChildLifecycle(parent = screenComponent.lifecycle)
        screenComponent.controller.onViewCreated(
            mainView = composeView,
            viewLifecycle = viewLifecycle
        )
        onDispose { viewLifecycle.destroy() }
    }
    composeView.model.value?.let { uiModel ->
        AnimeListScreen(
            uiModel = uiModel,
            dateFormatter = screenComponent.dateFormatter,
            dispatch = composeView::dispatch
        )
    }
}
