package com.alekseivinogradov.anoti.di.kmp

import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo

/**
 * Workaround for a kotlin-inject-anvil limitation on Kotlin/Native: without at least one
 * `@ContributesTo` in the same source set as [IosAppGraph]'s `@MergeComponent`, KSP silently
 * fails to find contributions from other modules on `iosArm64`/`iosSimulatorArm64` (Android is
 * unaffected). See https://github.com/amzn/kotlin-inject-anvil/issues/118 — the project is in
 * maintenance mode, so this stays until [IosAppGraph] contributes something of its own, or the
 * library fixes it upstream.
 *
 * Do not delete: without it, `:core-kmp:di`'s iOS klib compile fails to resolve dependency
 * bindings.
 */
@ContributesTo(AppScope::class)
interface IosAppGraphKspAnchor
