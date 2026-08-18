package com.alekseivinogradov.anoti.main.impl.presentation.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.AnimeFavoritesFragment
import com.alekseivinogradov.anoti.animelist.android.impl.presentation.AnimeListFragment
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.subscribe
import com.arkivanov.essenty.lifecycle.Lifecycle

/**
 * Keeps [containerId] showing the [Fragment] matching the currently active [NavRootChild]. There
 * is never more than one active child to show, and no back stack of fragments to manage, because
 * `NavRootComponent.navigateTo()` always replaces the whole stack.
 */
internal class NavRootChildFragmentBinder(
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) {
    fun bind(childStack: Value<ChildStack<NavRootConfig, NavRootChild>>, lifecycle: Lifecycle) {
        childStack.subscribe(lifecycle, ObserveLifecycleMode.CREATE_DESTROY) { stack ->
            val child = stack.active.instance
            // After a process-death restore the FragmentManager has already put the right
            // Fragment back on its own; replacing it would tear down a correct view for nothing.
            val isAlreadyShown =
                fragmentManager.findFragmentById(containerId)?.javaClass == fragmentClassFor(child)
            if (isAlreadyShown.not()) {
                fragmentManager.beginTransaction()
                    .replace(containerId, createFragment(child))
                    .commit()
            }
        }
    }

    private fun fragmentClassFor(child: NavRootChild): Class<out Fragment> = when (child) {
        is NavRootChild.List -> AnimeListFragment::class.java
        is NavRootChild.Favorites -> AnimeFavoritesFragment::class.java
    }

    private fun createFragment(child: NavRootChild): Fragment = when (child) {
        is NavRootChild.List -> AnimeListFragment()
        is NavRootChild.Favorites -> AnimeFavoritesFragment()
    }
}
