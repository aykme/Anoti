package com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.lifecycle

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.pause
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.essenty.lifecycle.stop

/**
 * A lifecycle that follows [parent] until it is destroyed on its own. It serves a part that can
 * end before its parent, such as one composition of a screen.
 *
 * Main thread only, like the lifecycles it follows.
 */
class ChildLifecycle(private val parent: Lifecycle) : Lifecycle {

    private val registry = LifecycleRegistry()

    private val parentCallbacks = object : Lifecycle.Callbacks {
        override fun onCreate() = registry.create()

        override fun onStart() = registry.start()

        override fun onResume() = registry.resume()

        override fun onPause() = registry.pause()

        override fun onStop() = registry.stop()

        override fun onDestroy() = registry.destroy()
    }

    init {
        // A destroyed parent replays nothing to a new subscriber.
        if (parent.state == Lifecycle.State.DESTROYED) {
            moveToDestroyed()
        } else {
            parent.subscribe(parentCallbacks)
        }
    }

    override val state: Lifecycle.State
        get() = registry.state

    override fun subscribe(callbacks: Lifecycle.Callbacks) = registry.subscribe(callbacks)

    override fun unsubscribe(callbacks: Lifecycle.Callbacks) = registry.unsubscribe(callbacks)

    /** Stops following [parent] and moves to destroyed. A second call does nothing. */
    fun destroy() {
        parent.unsubscribe(parentCallbacks)
        moveToDestroyed()
    }

    // The registry reaches destroyed only from created, so one never created is created first.
    // Its destroy callbacks then run, which is what closes whatever was built on it.
    private fun moveToDestroyed() {
        if (registry.state == Lifecycle.State.INITIALIZED) {
            registry.create()
        }
        registry.destroy()
    }
}
