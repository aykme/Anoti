package com.alekseivinogradov.anoti.animefavorites.android.impl.presentation

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.navigation.NavAnimeFavoritesScreenComponentHolder
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.AnimeFavoritesView
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.UiModel
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.AnimeFavoritesController
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.compose.AnimeFavoritesScreen
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.navigation.NavAnimeFavoritesScreenComponent
import com.alekseivinogradov.anoti.celebrity.android.impl.presentation.compose.ComposeFragment
import com.alekseivinogradov.anoti.celebrity.android.impl.presentation.edgetoedge.isEdgeToEdgeEnabled
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.ComposeMviView
import com.arkivanov.essenty.lifecycle.essentyLifecycle

class AnimeFavoritesFragment : ComposeFragment() {

    private lateinit var screenComponent: NavAnimeFavoritesScreenComponent

    // Needs an explicit AnimeFavoritesView supertype: the controller takes that interface, and
    // ComposeMviView's structural match to it isn't enough for Kotlin's nominal typing.
    private val composeView =
        object : ComposeMviView<UiModel, AnimeFavoritesMainStore.Intent>(), AnimeFavoritesView {}

    private var topInsetDp by mutableStateOf(0.dp)

    private val controller: AnimeFavoritesController by lazy {
        AnimeFavoritesController(
            lifecycle = screenComponent.lifecycle,
            mainStore = screenComponent.mainStore,
            animeDatabaseStore = screenComponent.animeDatabaseStore
        )
    }

    override val content: @Composable () -> Unit = {
        composeView.model.value?.let { uiModel ->
            AnimeFavoritesScreen(
                uiModel = uiModel,
                dateFormatter = screenComponent.dateFormatter,
                topInsetDp = topInsetDp,
                dispatch = composeView::dispatch
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Resolved here rather than in onAttach(): a restored Fragment is attached from inside
        // MainActivity.onCreate()'s super call, before the Activity has built its
        // NavRootComponent. onViewCreated() is the first callback guaranteed to run after
        // onCreate() has returned.
        screenComponent =
            (this.activity as NavAnimeFavoritesScreenComponentHolder).navAnimeFavoritesScreenComponent
        initEdgeToEdgeListenerIfNeeded(view)
        controller.onViewCreated(
            mainView = composeView,
            viewLifecycle = viewLifecycleOwner.essentyLifecycle()
        )
    }

    // Mirrors MainActivity.setSystemSettings()'s own edge-to-edge handling — the Activity
    // deliberately leaves top/bottom insets unconsumed at its root so each screen decides for
    // itself; this list needs its own top inset so the first item isn't drawn under the status
    // bar, the same job EdgeToEdgeItemDecorator did for the old RecyclerView.
    private fun initEdgeToEdgeListenerIfNeeded(view: View) {
        if (isEdgeToEdgeEnabled()) {
            ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
                val systemBarsTopPx = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
                topInsetDp = with(resources.displayMetrics) { (systemBarsTopPx / density).dp }
                insets
            }
        }
    }
}
