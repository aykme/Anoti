package com.alekseivinogradov.anoti.main.impl.presentation.compose

import androidx.compose.runtime.Immutable
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.controller.SystemMessageController
import com.alekseivinogradov.anoti.main.impl.presentation.navigation.NavRootChild
import com.alekseivinogradov.anoti.navigation.kmp.NavRootComponent
import com.arkivanov.essenty.lifecycle.Lifecycle

/**
 * The app-level navigation, store and system message references [RootContent] and
 * [BottomNavigationBarRoute] need, bundled together since they're always built and passed down as
 * one unit.
 */
// Every reference here is fixed for the host's whole lifetime. Without the annotation Compose
// reads the bundle as unstable, which costs both composables that take it their ability to skip.
@Immutable
internal class RootDependencies(
    val rootComponent: NavRootComponent<NavRootChild>,
    val mainStore: BottomNavigationBarStore,
    val animeDatabaseStore: AnimeDatabaseStore,
    val systemMessageController: SystemMessageController,
    val lifecycle: Lifecycle
)
