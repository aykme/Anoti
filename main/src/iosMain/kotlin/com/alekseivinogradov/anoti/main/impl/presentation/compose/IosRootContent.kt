package com.alekseivinogradov.anoti.main.impl.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import com.alekseivinogradov.anoti.main.impl.presentation.IosRootHolder

/**
 * The iOS screen: the same [RootContent] every host shows, over a saveable-state registry of
 * [holder]'s, so what the screen keeps with `rememberSaveable` reaches the scene's saved state.
 * A dialog's content is composed apart and keeps its own.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
internal fun IosRootContent(
    holder: IosRootHolder,
    dependencies: RootDependencies,
    notificationsRationale: NotificationsRationaleState
) {
    val registry = remember(holder) { holder.newSaveableStateRegistry() }
    CompositionLocalProvider(LocalSaveableStateRegistry provides registry) {
        RootContent(dependencies = dependencies, notificationsRationale = notificationsRationale)
    }
    // After the content on purpose: Compose forgets in reverse order, so this keeps the values
    // before the screen's own entries leave the registry.
    DisposableEffect(registry) {
        holder.attach(registry)
        onDispose { holder.detach(registry) }
    }
}
