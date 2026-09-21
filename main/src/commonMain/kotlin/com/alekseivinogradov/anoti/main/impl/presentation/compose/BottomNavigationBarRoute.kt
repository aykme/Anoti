package com.alekseivinogradov.anoti.main.impl.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.BottomNavigationBarView
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.mapper.mapStateToUiModel
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.BottomNavigationBarUiModel
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation.BottomNavigationBarController
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation.compose.BottomNavigationBar
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.ComposeMviView
import com.alekseivinogradov.anoti.main.impl.presentation.navigation.NavRootChild
import com.alekseivinogradov.anoti.navigation.kmp.NavRootComponent
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig

/**
 * Renders the bottom navigation bar and keeps its selected tab synced to [activeChild]. The
 * view/controller/store binding is created once for the Activity's lifetime, matching
 * [BottomNavigationBarStore]'s own lifetime.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
internal fun BottomNavigationBarRoute(dependencies: RootDependencies, activeChild: NavRootChild) {
    val rootComponent = dependencies.rootComponent
    val mainStore = dependencies.mainStore
    val composeView = remember {
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
    // Binding runs as an effect, not inside "remember": a discarded/retried
    // composition still executes "remember", which would start a second,
    // uncanceled MVIKotlin binder alongside the one from the composition that actually commits.
    LaunchedEffect(Unit) {
        BottomNavigationBarController(
            lifecycle = dependencies.lifecycle,
            mainStore = mainStore,
            animeDatabaseStore = dependencies.animeDatabaseStore
        ).onViewCreated(mainView = composeView, viewLifecycle = dependencies.lifecycle)
    }

    // Keeps the bar in step with later navigations. It cannot carry the first one: the binder
    // above attaches on a later main-thread message, so this first dispatch has no subscriber
    // yet and is dropped. The host sets the opening tab on the store itself for that reason.
    LaunchedEffect(activeChild) {
        composeView.dispatch(
            BottomNavigationBarStore.Intent.ChangeSelectedSection(
                selectedSection = activeChild.section
            )
        )
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
