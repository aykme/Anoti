package com.alekseivinogradov.anoti.main.impl.presentation.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.navigation.AnimeFavoritesRoute
import com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.navigation.AnimeListRoute
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.AnotiTheme
import com.alekseivinogradov.anoti.main.impl.presentation.navigation.NavRootChild
import com.alekseivinogradov.anoti.notificationsrationaledialog.kmp.api.presentation.compose.NotificationsRationaleDialog
import com.arkivanov.decompose.value.Value

/**
 * The whole app's single Compose tree: the active screen (switched by [dependencies]'s
 * navigation stack), the bottom navigation bar, and the notification-permission rationale
 * dialog overlay.
 *
 * @param edgeToEdgeEnabled whether the platform actually draws edge-to-edge — the platform
 * layer decides this (e.g. Android's OS-version gate) and passes only the plain result in, so
 * that reasoning doesn't leak into this otherwise-portable composable.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
internal fun RootContent(
    dependencies: RootDependencies,
    notificationsRationale: NotificationsRationaleState,
    edgeToEdgeEnabled: Boolean
) {
    AnotiTheme {
        val stack by dependencies.rootComponent.childStack.observeAsState()
        val activeChild = stack.active.instance
        val horizontalSystemBarsPadding = if (edgeToEdgeEnabled) {
            Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
        } else {
            Modifier
        }
        val topInsetDp = topInsetDp(edgeToEdgeEnabled)
        Column(modifier = Modifier.fillMaxSize().then(horizontalSystemBarsPadding)) {
            Box(modifier = Modifier.weight(1f)) {
                when (activeChild) {
                    is NavRootChild.List ->
                        AnimeListRoute(activeChild.component, topInsetDp)

                    is NavRootChild.Favorites ->
                        AnimeFavoritesRoute(activeChild.component, topInsetDp)
                }
            }
            BottomNavigationBarRoute(dependencies = dependencies, activeChild = activeChild)
        }
        NotificationsRationaleOverlay(notificationsRationale)
    }
}

// Screens pad their own top edge with this so content isn't drawn under the status bar; on API
// levels where edge-to-edge is disabled the system already reserves that space, so no padding is
// needed here.
@Composable
private fun topInsetDp(edgeToEdgeEnabled: Boolean): Dp =
    if (edgeToEdgeEnabled) {
        WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    } else {
        0.dp
    }

// A separate composable, not an `if` inline in RootContent: reading State.value here scopes
// recomposition to just this overlay instead of RootContent's whole body (the screen and the
// bottom nav bar included).
@Suppress("FunctionNaming")
@Composable
private fun NotificationsRationaleOverlay(state: NotificationsRationaleState) {
    if (state.visible.value) {
        NotificationsRationaleDialog(onDismiss = state.onDismiss, onApprove = state.onApprove)
    }
}

// Decompose's Value doesn't have a first-party Compose State bridge in the plain
// com.arkivanov.decompose:decompose artifact (only the separate extensions-compose one does) —
// this is that bridge's whole mechanism, kept local since RootContent is its only caller.
@Composable
private fun <T : Any> Value<T>.observeAsState(): State<T> {
    val state = remember { mutableStateOf(value) }
    DisposableEffect(this) {
        val cancellation = subscribe { state.value = it }
        onDispose { cancellation.cancel() }
    }
    return state
}
