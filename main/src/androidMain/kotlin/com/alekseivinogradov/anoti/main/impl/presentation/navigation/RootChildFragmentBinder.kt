package com.alekseivinogradov.anoti.main.impl.presentation.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.AnimeFavoritesFragment
import com.alekseivinogradov.anoti.animelist.android.impl.presentation.AnimeListFragment
import com.alekseivinogradov.anoti.navigation.kmp.RootConfig
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.subscribe
import com.arkivanov.essenty.lifecycle.Lifecycle

/**
 * Keeps [containerId] showing the [Fragment] matching the currently active [RootChild]. A plain
 * `replace()` per stack change is enough — `RootComponent.navigateTo()` always replaces the
 * whole stack, so there is never more than one active child to show, and no back stack of
 * fragments to manage.
 */
internal class RootChildFragmentBinder(
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) {
    fun bind(childStack: Value<ChildStack<RootConfig, RootChild>>, lifecycle: Lifecycle) {
        childStack.subscribe(lifecycle, ObserveLifecycleMode.CREATE_DESTROY) { stack ->
            fragmentManager.beginTransaction()
                .replace(containerId, fragmentFor(stack.active.instance))
                .commit()
        }
    }

    private fun fragmentFor(child: RootChild): Fragment = when (child) {
        is RootChild.List -> AnimeListFragment()
        is RootChild.Favorites -> AnimeFavoritesFragment()
    }
}
