package com.alekseivinogradov.anoti.main.impl.presentation

import android.view.View
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.model.SectionDomain
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.mapper.mapStateToUiModel
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.UiModel
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation.BottomNavigationBarView
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation.compose.BottomNavigationBar
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.ComposeMviView
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.anotiColorScheme
import com.alekseivinogradov.anoti.main.R
import com.alekseivinogradov.anoti.main.impl.presentation.navigation.NavRootChild
import com.alekseivinogradov.anoti.navigation.kmp.NavRootComponent
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.subscribe
import com.arkivanov.essenty.lifecycle.Lifecycle

internal class BottomNavigationBarViewImpl(
    rootView: View,
    mainStore: BottomNavigationBarStore,
    private val rootComponent: NavRootComponent<NavRootChild>,
    private val lifecycle: Lifecycle
) : ComposeMviView<UiModel, BottomNavigationBarStore.Intent>(
    initialModel = mapStateToUiModel(mainStore.state)
),
    BottomNavigationBarView {

    private val composeView: ComposeView = rootView.findViewById(R.id.bottom_nav_menu)

    init {
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            // anotiColorScheme(), not AnotiTheme: AnotiTheme's Surface(Modifier.fillMaxSize())
            // is meant for a full-screen ComposeFragment host and would stretch this
            // wrap_content bar to fill the whole window instead of sizing to its own content —
            // but a real color scheme (rather than MaterialTheme's own default) is still needed
            // so contentColorFor(Black) resolves to a real color instead of Color.Unspecified,
            // which otherwise leaves the tap ripple's color undefined.
            MaterialTheme(colorScheme = anotiColorScheme()) {
                model.value?.let { uiModel ->
                    BottomNavigationBar(uiModel = uiModel, dispatch = ::dispatch)
                }
            }
        }
    }

    override fun handle(label: BottomNavigationBarStore.Label) {
        when (label) {
            BottomNavigationBarStore.Label.NavigateToMain -> navigateToMain()
            BottomNavigationBarStore.Label.NavigateToFavorites -> navigateToFavorites()
        }
    }

    /**
     * Starts syncing the selected tab from [rootComponent]'s navigation state, for every
     * navigation change past construction time. Must be called only after this view's `events`
     * are already bound to the store (e.g. once `BottomNavigationBarController.onViewCreated`
     * returns) — [rootComponent]'s childStack fires its current value synchronously on
     * subscribe, and a dispatch with no bound subscriber yet is silently dropped. The state at
     * construction time doesn't depend on this binding being ready: this class's constructor
     * seeds it directly from the store's own synchronous state instead.
     */
    fun startObservingChildStack() {
        rootComponent.childStack.subscribe(lifecycle, ObserveLifecycleMode.CREATE_DESTROY) { stack ->
            // Assigned to a value so the `when` is an expression: adding a NavRootChild variant
            // then fails to compile here instead of silently doing nothing.
            val section: SectionDomain = when (stack.active.instance) {
                is NavRootChild.List -> SectionDomain.MAIN
                is NavRootChild.Favorites -> SectionDomain.FAVORITES
            }
            dispatch(
                BottomNavigationBarStore.Intent.ChangeSelectedSection(selectedSection = section)
            )
        }
    }

    private fun navigateToMain() {
        if (rootComponent.childStack.value.active.instance !is NavRootChild.List) {
            rootComponent.navigateTo(NavRootConfig.AnimeList)
        }
    }

    private fun navigateToFavorites() {
        if (rootComponent.childStack.value.active.instance !is NavRootChild.Favorites) {
            rootComponent.navigateTo(NavRootConfig.AnimeFavorites)
        }
    }
}
