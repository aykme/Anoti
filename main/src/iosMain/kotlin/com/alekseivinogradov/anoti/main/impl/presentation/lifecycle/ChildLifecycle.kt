package com.alekseivinogradov.anoti.main.impl.presentation.lifecycle

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.pause
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.essenty.lifecycle.stop

/**
 * A lifecycle that follows [parent] and can also be destroyed on its own, before [parent] ends.
 *
 * It exists for one case in [com.alekseivinogradov.anoti.main.impl.presentation.IosRootHolder].
 * A root restored from a broken saved state fails halfway, after its stores are already built.
 * The holder then builds a fresh root. Each attempt runs on its own child of the app's lifecycle,
 * so destroying that child closes the failed attempt's stores while the app keeps running.
 * Without it, they would stay subscribed to the database until the process ends. Android has no
 * such case: it never retries a failed restore.
 *
 * A lifecycle destroyed before it was ever created is created first. Its destroy callbacks then run
 * even when the app has not started yet. Main thread only, like the lifecycles it follows.
 */
internal class ChildLifecycle(private val parent: Lifecycle) : Lifecycle {

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
