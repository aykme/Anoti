package com.alekseivinogradov.anoti.navigation.ios

import com.alekseivinogradov.anoti.navigation.kmp.RootComponent
import com.alekseivinogradov.anoti.navigation.kmp.RootConfig
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry

/**
 * No iOS host app exists in this repo yet, so nothing calls [createRootComponent] at runtime
 * today (same situation as `IosAppGraph`, `core-kmp:di`). This function's role is to prove, at
 * klib-compile time, that [RootComponent] genuinely compiles and instantiates for iOS — a role
 * it keeps once a host app ships, alongside becoming its actual entry point.
 */
internal fun createRootComponent(): RootComponent<RootConfig> {
    val lifecycle = LifecycleRegistry()
    return RootComponent(
        componentContext = DefaultComponentContext(lifecycle = lifecycle),
        childFactory = { config, _ -> config }
    )
}
