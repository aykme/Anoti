package com.alekseivinogradov.anoti.di.kmp

import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo

/**
 * Empty contribution living in the same source set as [IosAppGraph]'s `@MergeComponent`. Works
 * around a known kotlin-inject-anvil limitation on Kotlin/Native targets: when a
 * `@MergeComponent`'s own source set has no `@ContributesTo` of its own, KSP's cross-module
 * contribution scan for `iosArm64`/`iosSimulatorArm64` silently fails to find contributions
 * declared in dependency modules, even though the same scan succeeds for the Android/JVM target.
 * See https://github.com/amzn/kotlin-inject-anvil/issues/118 (the project is in maintenance mode,
 * so no upstream fix is coming).
 *
 * Still required after Phase 9's cleanup — removing it was re-tested and
 * `:core-kmp:di:kspKotlinIosSimulatorArm64` fails again with `Cannot find an @Inject constructor
 * or provider for: io.ktor.client.HttpClient`.
 */
@ContributesTo(AppScope::class)
interface IosAppGraphKspAnchor
