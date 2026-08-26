package com.alekseivinogradov.anoti.animelist.android.impl.presentation

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alekseivinogradov.anoti.animelist.android.impl.presentation.navigation.NavAnimeListScreenComponent
import com.alekseivinogradov.anoti.animelist.android.impl.presentation.navigation.NavAnimeListScreenComponentHolder
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.AnimeListView
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.UiModel
import com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.AnimeListController
import com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.compose.AnimeListScreen
import com.alekseivinogradov.anoti.celebrity.android.impl.presentation.compose.ComposeFragment
import com.alekseivinogradov.anoti.celebrity.android.impl.presentation.edgetoedge.isEdgeToEdgeEnabled
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.ComposeMviView
import com.arkivanov.essenty.lifecycle.essentyLifecycle

class AnimeListFragment : ComposeFragment() {

    private lateinit var screenComponent: NavAnimeListScreenComponent

    // Needs an explicit AnimeListView supertype: the controller takes that interface, and
    // ComposeMviView's structural match to it isn't enough for Kotlin's nominal typing.
    private val composeView =
        object : ComposeMviView<UiModel, AnimeListMainStore.Intent>(), AnimeListView {}

    private var topInsetDp by mutableStateOf(0.dp)

    private val controller: AnimeListController by lazy {
        AnimeListController(
            lifecycle = screenComponent.lifecycle,
            mainStore = screenComponent.mainStore,
            animeDatabaseStore = screenComponent.animeDatabaseStore,
            ongoingSectionStore = screenComponent.ongoingSectionStore,
            announcedSectionStore = screenComponent.announcedSectionStore,
            searchSectionStore = screenComponent.searchSectionStore
        )
    }

    override val content: @Composable () -> Unit = {
        composeView.model.value?.let { uiModel ->
            AnimeListScreen(
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
            (this.activity as NavAnimeListScreenComponentHolder).navAnimeListScreenComponent
        initEdgeToEdgeListenerIfNeeded(view)
        controller.onViewCreated(
            mainView = composeView,
            viewLifecycle = viewLifecycleOwner.essentyLifecycle()
        )
    }

    // MainActivity leaves top/bottom insets unconsumed at its root so each screen decides for
    // itself; this screen needs its own top inset so the tabs/search bar isn't drawn under the
    // status bar.
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
