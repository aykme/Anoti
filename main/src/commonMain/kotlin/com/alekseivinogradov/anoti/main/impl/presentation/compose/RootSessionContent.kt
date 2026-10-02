package com.alekseivinogradov.anoti.main.impl.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import com.alekseivinogradov.anoti.main.impl.presentation.RootSession

/**
 * The root content of a screen host whose root lives as long as one composition. Each generation
 * of [session] gets a new root, shown by the same [RootContent] every host shows. What the
 * screen saves with `rememberSaveable` goes into the root's own registry, which the session
 * saves with the root. A dialog's content on iOS is composed apart and keeps its own. The root's
 * notification-permission check runs once it is built.
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
internal fun RootSessionContent(session: RootSession) {
    key(RootGenerationKey(session.generation)) {
        val root = remember(session) { RememberedRoot(session) }.root
        // Replaces the host's registry, so the values are saved with the root's state keeper.
        CompositionLocalProvider(LocalSaveableStateRegistry provides root.saveableStateRegistry) {
            RootContent(
                dependencies = root.host.dependencies,
                notificationsRationale = root.host.notificationsRationale
            )
        }
        LaunchedEffect(root) {
            session.checkNotificationPermission(root)
        }
    }
}
