package com.alekseivinogradov.anoti.animenotification.ios.impl.di

import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * iOS-only merge component that exists solely to exercise kotlin-inject-anvil's [AppScope] merge
 * for [AnimeNotificationManager] at klib-compile time, per the migration plan's "iOS DI must
 * compile at every phase" constraint.
 *
 * Android's [AnimeNotificationManager] binding stays on Dagger (`AnimeNotificationModule`) until
 * Phase 9, so it cannot yet be declared as an `abstract val` on the shared, Android+iOS
 * `TransitionalAppGraph` (`core-kmp:di`) — that class is compiled for both platforms from a
 * single commonMain source set, and kotlin-inject-anvil's merge step requires a real binding on
 * *every* compiled target for every declared accessor, which would force a matching (and
 * currently nonexistent) Android kotlin-inject binding. This small, self-contained, iOS-only
 * graph is the deliberate substitute: it proves the iOS binding resolves without touching
 * Android's still-Dagger-owned wiring. Superseded once Android's `AnimeNotificationManager`
 * migrates in Phase 9, at which point this accessor folds into the real app-scope graph.
 */
@MergeComponent(AppScope::class)
@SingleIn(AppScope::class)
interface AnimeNotificationIosGraph {
    val animeNotificationManager: AnimeNotificationManager
}
