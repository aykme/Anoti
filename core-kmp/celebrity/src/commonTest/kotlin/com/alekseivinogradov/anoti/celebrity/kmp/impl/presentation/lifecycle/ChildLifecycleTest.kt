package com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.lifecycle

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChildLifecycleTest {

    @Test
    fun catchesUpWithAParentAlreadyResumed() {
        //Given
        val parent = LifecycleRegistry().apply { resume() }

        //When
        val child = ChildLifecycle(parent = parent)

        //Then
        assertEquals(Lifecycle.State.RESUMED, child.state)
    }

    @Test
    fun followsTheParentDownAndUp() {
        //Given
        val parent = LifecycleRegistry().apply { resume() }
        val child = ChildLifecycle(parent = parent)

        //When
        parent.stop()
        val stopped = child.state
        parent.resume()

        //Then
        assertEquals(Lifecycle.State.CREATED, stopped)
        assertEquals(Lifecycle.State.RESUMED, child.state)
    }

    @Test
    fun destroyingItLeavesTheParentRunning() {
        //Given
        val parent = LifecycleRegistry().apply { resume() }
        val child = ChildLifecycle(parent = parent)

        //When
        child.destroy()

        //Then
        assertEquals(Lifecycle.State.DESTROYED, child.state)
        assertEquals(Lifecycle.State.RESUMED, parent.state)
    }

    @Test
    fun destroyingItStopsFollowingTheParent() {
        //Given
        val parent = SubscriptionsLifecycleFake()
        val child = ChildLifecycle(parent = parent)

        //When
        child.destroy()

        //Then
        assertTrue(parent.callbacks.isEmpty())
    }

    @Test
    fun endsWithTheParent() {
        //Given
        val parent = LifecycleRegistry().apply { resume() }
        val child = ChildLifecycle(parent = parent)

        //When
        parent.destroy()
        child.destroy()

        //Then
        assertEquals(Lifecycle.State.DESTROYED, child.state)
    }

    @Test
    fun isDestroyedAtOnceUnderAParentAlreadyDestroyed() {
        //Given
        // destroy() alone leaves a registry that was never created where it was.
        val parent = LifecycleRegistry().apply {
            create()
            destroy()
        }

        //When
        val child = ChildLifecycle(parent = parent)

        //Then
        assertEquals(Lifecycle.State.DESTROYED, child.state)
    }

    @Test
    fun destroyingItBeforeTheParentStartsStillRunsItsDestroyCallbacks() {
        //Given
        val parent = LifecycleRegistry()
        val child = ChildLifecycle(parent = parent)
        var destroyed = false
        child.doOnDestroy { destroyed = true }

        //When
        child.destroy()

        //Then
        assertEquals(Lifecycle.State.DESTROYED, child.state)
        assertTrue(destroyed)
    }
}

/** A parent that only records who follows it. */
private class SubscriptionsLifecycleFake : Lifecycle {

    val callbacks = mutableListOf<Lifecycle.Callbacks>()

    override val state: Lifecycle.State = Lifecycle.State.RESUMED

    override fun subscribe(callbacks: Lifecycle.Callbacks) {
        this.callbacks += callbacks
    }

    override fun unsubscribe(callbacks: Lifecycle.Callbacks) {
        this.callbacks -= callbacks
    }
}
