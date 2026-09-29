package com.alekseivinogradov.anoti.main.impl.presentation

import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher

/**
 * One root a [RootSession] built.
 *
 * @param number counts the roots of its session from 1, for the log.
 * @param host the root's navigation, stores and notification-permission flow.
 * @param lifecycle what the root lives by. Ending it ends the root.
 * @param stateKeeper saves the root's state, [saveableStateRegistry]'s values included.
 * @param saveableStateRegistry keeps what the root's composition saves with `rememberSaveable`.
 */
internal class SessionRoot(
    val number: Int,
    val host: RootHost,
    val lifecycle: RootLifecycle,
    val stateKeeper: StateKeeperDispatcher,
    val saveableStateRegistry: SaveableStateRegistry
)
