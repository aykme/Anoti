package com.alekseivinogradov.anoti.main.impl.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.BottomNavigationBarView
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.mapper.mapStateToUiModel
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.BottomNavigationBarUiModel
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation.compose.BottomNavigationBar
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.ComposeMviView
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.lifecycle.ChildLifecycle
import com.alekseivinogradov.anoti.main.impl.presentation.navigation.NavRootChild
import com.alekseivinogradov.anoti.navigation.kmp.NavRootComponent
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig

/**
 * Renders the bottom navigation bar. Each composition gets its own view, bound to the root's bar
 * controller for as long as the composition lasts. The selected tab comes from the store, which
 * the root keeps on the screen the stack holds.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
internal fun BottomNavigationBarRoute(dependencies: RootDependencies) {
    val rootComponent = dependencies.rootComponent
    val mainStore = dependencies.mainStore
    val composeView = remember(dependencies) {
        object :
            ComposeMviView<BottomNavigationBarUiModel, BottomNavigationBarStore.Intent>(
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
    // An effect, not "remember": a discarded composition still runs "remember", and its binder
    // would never be stopped.
    DisposableEffect(dependencies) {
        val viewLifecycle = ChildLifecycle(parent = dependencies.lifecycle)
        dependencies.barController.onViewCreated(
            mainView = composeView,
            viewLifecycle = viewLifecycle
        )
        onDispose { viewLifecycle.destroy() }
    }

    composeView.model.value?.let { uiModel ->
        BottomNavigationBar(uiModel = uiModel, dispatch = composeView::dispatch)
    }
}

// Tapping the tab that is already open would otherwise still run a navigation transaction. The
// stack would come back holding the same child, so nothing downstream can tell the difference.
private fun navigateTo(rootComponent: NavRootComponent<NavRootChild>, target: NavRootConfig) {
    if (rootComponent.childStack.value.active.instance.config != target) {
        rootComponent.navigateTo(target)
    }
}
