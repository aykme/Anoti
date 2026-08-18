package com.alekseivinogradov.anoti.di.kmp.qualifier

import me.tatarka.inject.annotations.Qualifier

/** Marks the app-wide [PlatformContext][com.alekseivinogradov.anoti.di.kmp.PlatformContext]. */
@Qualifier
annotation class AppContext

/**
 * Defines the anime background-update feature's own `WorkManager` bindings (its
 * `Configuration`, one-time and periodic `WorkRequest`s) from any other same-typed binding in
 * the graph.
 */
@Qualifier
annotation class AnimeBackgroundUpdate
