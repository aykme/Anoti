package com.alekseivinogradov.anoti.main.impl.presentation.compose

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.main.impl.presentation.navigation.NavRootChild
import com.alekseivinogradov.anoti.navigation.kmp.NavRootComponent
import com.arkivanov.essenty.lifecycle.Lifecycle

/**
 * The app-level navigation and store references [RootContent] and [BottomNavigationBarRoute]
 * both need, bundled together since they're always built and passed down as one unit.
 */
internal class RootDependencies(
    val rootComponent: NavRootComponent<NavRootChild>,
    val mainStore: BottomNavigationBarStore,
    val animeDatabaseStore: AnimeDatabaseStore,
    val lifecycle: Lifecycle
)
