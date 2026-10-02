package com.alekseivinogradov.anoti.main.impl.presentation.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.IntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.offset
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.navigation.AnimeFavoritesRoute
import com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.navigation.AnimeListRoute
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose.AnotiTheme
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose.SystemMessageHost
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose.horizontalSystemBarsPadding
import com.alekseivinogradov.anoti.main.impl.presentation.navigation.NavRootChild
import com.alekseivinogradov.anoti.notificationsrationaledialog.kmp.impl.presentation.compose.NotificationsRationaleDialog
import com.arkivanov.decompose.extensions.compose.stack.Children
import kotlin.math.max

/**
 * The whole app's single Compose tree: the active screen (switched by [dependencies]'s
 * navigation stack), the bottom navigation bar and the notification-permission rationale dialog
 * overlay. The system message host is drawn over the screen and the bottom bar.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
internal fun RootContent(
    dependencies: RootDependencies,
    notificationsRationale: NotificationsRationaleState
) {
    AnotiTheme {
        val bottomBarHeight = remember { mutableIntStateOf(0) }
        Column(modifier = Modifier.fillMaxSize()) {
            // Children keeps each screen's rememberSaveable values under that screen's own key, and
            // drops them once the screen leaves the stack.
            Children(
                stack = dependencies.rootComponent.childStack,
                modifier = Modifier.weight(1f)
            ) { child ->
                when (val screen = child.instance) {
                    is NavRootChild.List -> AnimeListRoute(screen.component)
                    is NavRootChild.Favorites -> AnimeFavoritesRoute(screen.component)
                }
            }
            Box(
                modifier = Modifier.onSizeChanged { size: IntSize ->
                    bottomBarHeight.intValue = size.height
                }
            ) {
                BottomNavigationBarRoute(dependencies = dependencies)
            }
        }
        NotificationsRationaleOverlay(notificationsRationale)
        SystemMessageHost(
            controller = dependencies.systemMessageController,
            modifier = Modifier.aboveBottomBarAndKeyboard(bottomBarHeight)
        )
    }
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

// Read in the layout phase only, so the bar resizing or the keyboard moving never recomposes.
@Composable
private fun Modifier.aboveBottomBarAndKeyboard(bottomBarHeight: IntState): Modifier {
    val ime = WindowInsets.ime
    return horizontalSystemBarsPadding().layout { measurable: Measurable, constraints: Constraints ->
        val bottomOffset = max(bottomBarHeight.intValue, ime.getBottom(this))
        val placeable = measurable.measure(
            constraints.copy(minWidth = 0, minHeight = 0).offset(vertical = -bottomOffset)
        )
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(
                x = (constraints.maxWidth - placeable.width) / 2,
                y = constraints.maxHeight - bottomOffset - placeable.height
            )
        }
    }
}
