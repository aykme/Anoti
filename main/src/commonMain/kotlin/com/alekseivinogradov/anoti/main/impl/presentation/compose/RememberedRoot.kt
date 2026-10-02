package com.alekseivinogradov.anoti.main.impl.presentation.compose

import androidx.compose.runtime.RememberObserver
import com.alekseivinogradov.anoti.main.impl.presentation.RootSession
import com.alekseivinogradov.anoti.main.impl.presentation.SessionRoot

/**
 * Builds a root of [session] and ends it when the composition lets go of it. A composition that
 * is abandoned before it applies ends its root as well, so nothing it built stays running.
 */
internal class RememberedRoot(private val session: RootSession) : RememberObserver {

    /** The root built for this composition. */
    val root: SessionRoot = session.createRoot()

    override fun onRemembered() = Unit

    override fun onForgotten() = session.endRoot(root)

    override fun onAbandoned() = session.endRoot(root)
}
