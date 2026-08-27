package com.alekseivinogradov.anoti.celebrity.android.impl.presentation.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.alekseivinogradov.anoti.celebrity.android.impl.presentation.edgetoedge.isEdgeToEdgeEnabled
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.AnotiTheme

/**
 * Base [Fragment] for a screen whose UI is written in Compose.
 *
 * A temporary bridge for hosting Compose UI inside `Fragment`-based navigation; removed once
 * every screen is on Compose without `Fragment`. [content] is rendered inside [AnotiTheme]
 * automatically, and [topInsetDp] is kept up to date with the status bar's height for
 * subclasses that need it.
 */
abstract class ComposeFragment : Fragment() {

    /**
     * The screen's Compose UI.
     */
    abstract val content: @Composable () -> Unit

    /**
     * The status bar's height, for a subclass's [content] to pad its top-level composable with
     * so it isn't drawn underneath it.
     */
    protected var topInsetDp by mutableStateOf(0.dp)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEdgeToEdgeListenerIfNeeded(view)
    }

    // MainActivity leaves top/bottom insets unconsumed at its root so each screen decides for
    // itself; this sets the top inset so content isn't drawn under the status bar.
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
