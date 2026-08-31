package com.alekseivinogradov.anoti.main.impl.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.model.SectionDomain
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.mapper.mapStateToUiModel
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.UiModel
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation.BottomNavigationBarController
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation.BottomNavigationBarView
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation.compose.BottomNavigationBar
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.ComposeMviView
import com.alekseivinogradov.anoti.main.impl.presentation.navigation.NavRootChild
import com.alekseivinogradov.anoti.navigation.kmp.NavRootComponent
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import com.arkivanov.essenty.lifecycle.Lifecycle

/**
 * Renders the bottom navigation bar and keeps its selected tab synced to [activeChild]. The
 * view/controller/store binding is created once for the Activity's lifetime, matching
 * [BottomNavigationBarStore]'s own lifetime.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
internal fun BottomNavigationBarRoute(
    rootComponent: NavRootComponent<NavRootChild>,
    mainStore: BottomNavigationBarStore,
    animeDatabaseStore: AnimeDatabaseStore,
    lifecycle: Lifecycle,
    activeChild: NavRootChild
) {
    val composeView = remember {
        object :
            ComposeMviView<UiModel, BottomNavigationBarStore.Intent>(
                initialModel = mapStateToUiModel(mainStore.state)
            ),
            BottomNavigationBarView {
            override fun handle(label: BottomNavigationBarStore.Label) {
                when (label) {
                    BottomNavigationBarStore.Label.NavigateToMain ->
                        navigateTo(rootComponent, NavRootConfig.AnimeList)

                    BottomNavigationBarStore.Label.NavigateToFavorites ->
                        navigateTo(rootComponent, NavRootConfig.AnimeFavorites)
                }
            }
        }
    }
    // Binding runs as an effect, not inside "remember": a discarded/retried
    // composition still executes "remember", which would start a second,
    // uncanceled MVIKotlin binder alongside the one from the composition that actually commits.
    LaunchedEffect(Unit) {
        BottomNavigationBarController(
            lifecycle = lifecycle,
            mainStore = mainStore,
            animeDatabaseStore = animeDatabaseStore
        ).onViewCreated(mainView = composeView, viewLifecycle = lifecycle)
    }

    // onViewCreated above has no suspension point, so this effect (declared after it) only ever
    // starts once that binding has fully completed — a dispatch with no bound subscriber yet is
    // silently dropped.
    LaunchedEffect(activeChild) {
        val section = when (activeChild) {
            is NavRootChild.List -> SectionDomain.MAIN
            is NavRootChild.Favorites -> SectionDomain.FAVORITES
        }
        composeView.dispatch(
            BottomNavigationBarStore.Intent.ChangeSelectedSection(selectedSection = section)
        )
    }

    composeView.model.value?.let { uiModel ->
        BottomNavigationBar(uiModel = uiModel, dispatch = composeView::dispatch)
    }
}

private fun navigateTo(rootComponent: NavRootComponent<NavRootChild>, target: NavRootConfig) {
    val alreadyActive = when (target) {
        NavRootConfig.AnimeList -> rootComponent.childStack.value.active.instance is NavRootChild.List
        NavRootConfig.AnimeFavorites ->
            rootComponent.childStack.value.active.instance is NavRootChild.Favorites
    }
    if (alreadyActive.not()) {
        rootComponent.navigateTo(target)
    }
}
