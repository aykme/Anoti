package com.alekseivinogradov.anoti.main.impl.presentation

/**
 * One root a [RootSession] built.
 *
 * @param number counts the roots of its session from 1, for the log.
 * @param host the root's navigation, stores and notification-permission flow.
 * @param lifecycle what the root lives by. Ending it ends the root.
 */
internal class SessionRoot(
    val number: Int,
    val host: RootHost,
    val lifecycle: RootLifecycle
)
