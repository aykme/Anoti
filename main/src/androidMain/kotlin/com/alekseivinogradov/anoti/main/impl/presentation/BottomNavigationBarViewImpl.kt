package com.alekseivinogradov.anoti.main.impl.presentation

import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.model.SectionDomain
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.UiModel
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation.BottomNavigationBarView
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation.compose.BottomNavigationBar
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.AnotiTheme
import com.alekseivinogradov.anoti.main.R
import com.alekseivinogradov.anoti.main.impl.presentation.navigation.NavRootChild
import com.alekseivinogradov.anoti.navigation.kmp.NavRootComponent
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.subscribe
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer

internal class BottomNavigationBarViewImpl(
    rootView: View,
    private val rootComponent: NavRootComponent<NavRootChild>,
    private val lifecycle: Lifecycle
) : BottomNavigationBarView, BaseMviView<UiModel, BottomNavigationBarStore.Intent>() {

    private val composeView: ComposeView = rootView.findViewById(R.id.bottom_nav_menu)

    private var uiModel by mutableStateOf(UiModel())

    init {
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            AnotiTheme {
                BottomNavigationBar(uiModel = uiModel, dispatch = ::dispatch)
            }
        }
        initOnDestinationChangeListener()
    }

    override val renderer: ViewRenderer<UiModel> = diff {
        diff(get = { it }, set = { newUiModel -> uiModel = newUiModel })
    }

    override fun handle(label: BottomNavigationBarStore.Label) {
        when (label) {
            BottomNavigationBarStore.Label.NavigateToMain -> navigateToMain()
            BottomNavigationBarStore.Label.NavigateToFavorites -> navigateToFavorites()
        }
    }

    private fun initOnDestinationChangeListener() {
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
