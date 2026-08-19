package com.alekseivinogradov.anoti.celebrity.android.impl.presentation.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment

/**
 * Base [Fragment] for a screen whose UI is written in Compose.
 *
 * A temporary bridge for hosting Compose UI inside `Fragment`-based navigation; removed once
 * every screen is on Compose without `Fragment`. [content] is rendered inside [AnotiTheme]
 * automatically.
 */
abstract class ComposeFragment : Fragment() {

    /**
     * The screen's Compose UI.
     */
    abstract val content: @Composable () -> Unit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            AnotiTheme {
                content()
            }
        }
    }
}
