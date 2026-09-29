package com.alekseivinogradov.anoti.main.impl.presentation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import com.alekseivinogradov.anoti.main.impl.di.DiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionRequests
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionStatus
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher

private const val TAG = "RootSession"

/**
 * The successive roots of a screen host whose root lives exactly as long as one composition. A
 * root kept across compositions would have its screens bind a second time.
 *
 * Main thread only. Compose reads nothing from it but [generation], a snapshot state.
 *
 * @param createDiRootComponent builds the graph of a new root.
 * @param createLifecycle builds the lifecycle of a new root.
 * @param notificationPermissionRequests the platform's ways of getting the permission granted.
 * @param readNotificationPermissionStatus reads what the platform reports about the permission.
 */
@Stable
internal class RootSession(
    private val createDiRootComponent: () -> DiRootComponent,
    private val createLifecycle: () -> RootLifecycle,
    private val notificationPermissionRequests: NotificationPermissionRequests,
    private val readNotificationPermissionStatus: suspend () -> NotificationPermissionStatus
) {

    private val generationState = mutableIntStateOf(0)

    /** Changes whenever the current root has to be replaced by a new one. */
    val generation: Int
        get() = generationState.intValue

    /** The root built last, until it ends. */
    var currentRoot: SessionRoot? = null
        private set

    // The screen a tapped notification asked for, until a root opens on it.
    private var pendingTarget: NavRootConfig? = null

    // Only numbers the roots in the log.
    private var builtRoots = 0

    /**
     * Builds a new root and makes it the current one. A root built after a notification tap opens
     * on the screen the tap asked for, with saved state discarded. Later roots no longer see it.
     */
    fun createRoot(): SessionRoot {
        val openingTarget = pendingTarget
        pendingTarget = null
        val lifecycle = createLifecycle()
        val host = RootHost(
            diRootComponent = createDiRootComponent(),
            openingTarget = openingTarget,
            createComponentContext = { _: Boolean ->
                DefaultComponentContext(
                    lifecycle = lifecycle.lifecycle,
                    stateKeeper = StateKeeperDispatcher()
                )
            },
            notificationPermissionRequests = notificationPermissionRequests
        )
        builtRoots++
        println("$TAG: built root $builtRoots")
        return SessionRoot(number = builtRoots, host = host, lifecycle = lifecycle)
            .also { currentRoot = it }
    }

    /** Ends [root]. The current root, if it is another one, stays as it is. */
    fun endRoot(root: SessionRoot) {
        root.lifecycle.end()
        if (currentRoot === root) {
            currentRoot = null
        }
        println("$TAG: ended root ${root.number}")
    }

    /**
     * Opens the app on [target], the screen a tapped notification names. A cold tap and a warm one
     * are alike: the next root opens on it, and a live root is replaced by a new one at once.
     */
    fun openFromNotification(target: NavRootConfig) {
        pendingTarget = target
        val rebuilds = currentRoot != null
        if (rebuilds) {
            generationState.intValue++
        }
        println("$TAG: a notification opens $target, rebuilding: $rebuilds")
    }

    /**
     * Reads the notification permission for [root] and acts on it. A status that arrives once
     * [root] is no longer the current one is dropped.
     */
    suspend fun checkNotificationPermission(root: SessionRoot) {
        val status = readNotificationPermissionStatus()
        if (currentRoot === root) {
            root.host.onNotificationPermissionStatus(status)
        }
    }
}
