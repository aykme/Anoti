package com.alekseivinogradov.anoti.navigation.kmp

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value

/**
 * Owns the app's root navigation stack. Always holds exactly one active [RootConfig] —
 * [navigateTo] replaces the whole stack rather than pushing onto it, so the back stack never
 * grows and a screen is always freshly created when navigated to (the previous one is disposed).
 *
 * This class knows nothing about what a [Child] actually is — that's supplied by the caller via
 * [childFactory], so it stays reusable across platforms without depending on any feature module.
 *
 * @param componentContext the context this component is attached to — on Android, the
 * `defaultComponentContext()` extension; elsewhere,
 * `DefaultComponentContext(lifecycle = LifecycleRegistry())`.
 * @param initialConfiguration the [RootConfig] to start from when there is no saved state to
 * restore (or when the caller explicitly discarded it, e.g. for a deep link).
 * @param childFactory builds a [Child] for a given [RootConfig] and its own [ComponentContext].
 */
class RootComponent<out Child : Any>(
    componentContext: ComponentContext,
    initialConfiguration: RootConfig = RootConfig.AnimeList,
    childFactory: (config: RootConfig, componentContext: ComponentContext) -> Child
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<RootConfig>()

    val childStack: Value<ChildStack<RootConfig, Child>> = childStack(
        source = navigation,
        serializer = RootConfig.serializer(),
        initialConfiguration = initialConfiguration,
        handleBackButton = true,
        childFactory = childFactory
    )

    /** Replaces the entire stack with [target] — the previous screen is destroyed. */
    fun navigateTo(target: RootConfig) {
        navigation.replaceAll(target)
    }
}
