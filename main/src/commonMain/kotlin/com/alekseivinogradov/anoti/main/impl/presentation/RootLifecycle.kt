package com.alekseivinogradov.anoti.main.impl.presentation

import com.arkivanov.essenty.lifecycle.Lifecycle

/**
 * The lifecycle one root lives by, with the call that ends it. A [Lifecycle] cannot end itself,
 * and each platform's own lifecycle is ended its own way.
 *
 * @param lifecycle drives the root's screens while it lives.
 * @param end moves [lifecycle] to destroyed, which disposes the root's stores and screens.
 */
internal class RootLifecycle(
    val lifecycle: Lifecycle,
    val end: () -> Unit
)
