package com.alekseivinogradov.anoti.animebackgroundupdate.ios.impl.di

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.scheduler.AnimeBackgroundScheduler
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.MergeComponent
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * iOS-only merge component that exists solely to exercise kotlin-inject-anvil's [AppScope] merge
 * for [AnimeBackgroundScheduler] at klib-compile time, per the migration plan's "iOS DI must
 * compile at every phase" constraint.
 *
 * Android's [AnimeBackgroundScheduler]/`AnimeUpdateManager` bindings stay on Dagger until
 * Phase 9, so they cannot yet be declared as `abstract val`s on the shared, Android+iOS
 * `TransitionalAppGraph` (`core-kmp:di`) — that class is compiled for both platforms from a
 * single commonMain source set, and kotlin-inject-anvil's merge step requires a real binding on
 * *every* compiled target for every declared accessor, which would force a matching (and
 * currently nonexistent) Android kotlin-inject binding. This small, self-contained, iOS-only
 * graph is the deliberate substitute: it proves the iOS binding chain resolves without touching
 * Android's still-Dagger-owned wiring. Superseded once Android's `AnimeUpdateManager` migrates
 * in Phase 9, at which point this accessor folds into the real app-scope graph.
 */
@MergeComponent(AppScope::class)
@SingleIn(AppScope::class)
interface AnimeBackgroundUpdateIosGraph {
    val animeBackgroundScheduler: AnimeBackgroundScheduler
}
