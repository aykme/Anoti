package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.AnimeListView
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.UiModel
import com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.AnimeListController
import com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.compose.AnimeListScreen
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.ComposeMviView

/**
 * Renders the anime-list screen for as long as [screenComponent] stays the active root child —
 * a new [screenComponent] instance (after navigating away and back) gets its own fresh view and
 * controller, matching the store's own per-activation lifetime.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
fun AnimeListRoute(screenComponent: NavAnimeListScreenComponent, topInsetDp: Dp) {
    // Needs an explicit AnimeListView supertype: the controller takes that interface, and
    // ComposeMviView's structural match to it isn't enough for Kotlin's nominal typing.
    val composeView = remember(screenComponent) {
        object : ComposeMviView<UiModel, AnimeListMainStore.Intent>(), AnimeListView {}
    }
    // Binding runs as an effect, not inside remember's calculator: a discarded/retried
    // composition still executes remember's calculator, which would start a second,
    // un-cancelled MVIKotlin binder alongside the one from the composition that actually commits.
    LaunchedEffect(screenComponent) {
        AnimeListController(
            lifecycle = screenComponent.lifecycle,
            mainStore = screenComponent.mainStore,
            animeDatabaseStore = screenComponent.animeDatabaseStore,
            ongoingSectionStore = screenComponent.ongoingSectionStore,
            announcedSectionStore = screenComponent.announcedSectionStore,
            searchSectionStore = screenComponent.searchSectionStore
        ).onViewCreated(mainView = composeView, viewLifecycle = screenComponent.lifecycle)
    }
    composeView.model.value?.let { uiModel ->
        AnimeListScreen(
            uiModel = uiModel,
            dateFormatter = screenComponent.dateFormatter,
            topInsetDp = topInsetDp,
            dispatch = composeView::dispatch
        )
    }
}
