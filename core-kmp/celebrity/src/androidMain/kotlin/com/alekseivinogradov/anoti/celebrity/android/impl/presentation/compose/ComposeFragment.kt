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
                applyTopInset(insets)
                insets
            }
            // On a screen's first-ever attach, this fragment's view isn't attached to the window
            // yet, and the window never redelivers insets to it once it does attach. Only a
            // fragment swap, which recreates the view, happens to trigger a fresh dispatch.
            // So the listener above would otherwise never fire here. Reading the window's
            // already-cached insets directly, as soon as the view attaches, sidesteps that.
            if (view.isAttachedToWindow) {
                ViewCompat.getRootWindowInsets(view)?.let(::applyTopInset)
            } else {
                view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(attachedView: View) {
                        attachedView.removeOnAttachStateChangeListener(this)
                        ViewCompat.getRootWindowInsets(attachedView)?.let(::applyTopInset)
                    }

                    override fun onViewDetachedFromWindow(detachedView: View) = Unit
                })
            }
        }
    }

    private fun applyTopInset(insets: WindowInsetsCompat) {
        val systemBarsTopPx = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
        topInsetDp = with(resources.displayMetrics) { (systemBarsTopPx / density).dp }
    }
}
