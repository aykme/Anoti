package com.alekseivinogradov.anoti.main.impl.presentation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.alekseivinogradov.anoti.main.impl.di.DiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionRequests
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionStatus
import com.alekseivinogradov.anoti.main.impl.presentation.savedstate.SaveableStateCodec
import com.alekseivinogradov.anoti.main.impl.presentation.savedstate.SavedStateStorage
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import kotlinx.serialization.json.JsonElement

private const val TAG = "RootSession"

// The key the root's saveable-state registry is kept under in its state keeper.
private const val SAVEABLE_STATE_KEY = "RootSaveableState"

/**
 * The successive roots of a screen host whose root lives exactly as long as one composition. A
 * root kept across compositions would have its screens bind a second time.
 *
 * It also saves the current root's state, for a platform that saves none across the end of its
 * process, and restores it into the next root it builds.
 *
 * Main thread only. Compose reads nothing from it but [generation], a snapshot state.
 *
 * @param createDiRootComponent builds the graph of a new root.
 * @param createLifecycle builds the lifecycle of a new root.
 * @param notificationPermissionRequests the platform's ways of getting the permission granted.
 * @param readNotificationPermissionStatus reads what the platform reports about the permission.
 * @param savedStateStorage keeps the state between processes.
 */
@Stable
internal class RootSession(
    private val createDiRootComponent: () -> DiRootComponent,
    private val createLifecycle: () -> RootLifecycle,
    private val notificationPermissionRequests: NotificationPermissionRequests,
    private val readNotificationPermissionStatus: suspend () -> NotificationPermissionStatus,
    private val savedStateStorage: SavedStateStorage
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
     * Builds a new root and makes it the current one. It restores the saved state when that
     * state is still valid. A root built after a notification tap opens on the screen the tap
     * asked for, with saved state discarded. Later roots no longer see the tap.
     *
     * The saved state is taken either way. A state that crashes the app on restore is gone by
     * then, so it crashes the app once.
     */
    fun createRoot(): SessionRoot {
        val openingTarget = pendingTarget
        pendingTarget = null
        val savedState = savedStateStorage.take(isDiscarded = openingTarget != null)
        val lifecycle = createLifecycle()
        var stateKeeper: StateKeeperDispatcher? = null
        val host = RootHost(
            diRootComponent = createDiRootComponent(),
            openingTarget = openingTarget,
            createComponentContext = { discardSavedState: Boolean ->
                val keeper = createStateKeeper(savedState.takeUnless { discardSavedState })
                stateKeeper = keeper
                DefaultComponentContext(lifecycle = lifecycle.lifecycle, stateKeeper = keeper)
            },
            notificationPermissionRequests = notificationPermissionRequests
        )
        val rootStateKeeper = checkNotNull(stateKeeper)
        builtRoots++
        println("$TAG: built root $builtRoots")
        return SessionRoot(
            number = builtRoots,
            host = host,
            lifecycle = lifecycle,
            stateKeeper = rootStateKeeper,
            saveableStateRegistry = createSaveableStateRegistry(rootStateKeeper)
        ).also { currentRoot = it }
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
     * Saves the current root's state for the next process. Without a root nothing is saved, and
     * an older state stays.
     */
    fun saveState() {
        val root = currentRoot
        if (root == null) {
            println("$TAG: nothing saved, there is no root")
            return
        }
        savedStateStorage.save(rootNumber = root.number, state = root.stateKeeper::save)
    }

    /**
     * Drops the saved state once the app is back in front with a live root. That root moves on
     * from the saved screen, and a crash would otherwise reopen the older one. Without a root
     * the state stays for the root still to be built.
     */
    fun dropSavedStateOfLiveRoot() {
        if (currentRoot != null) {
            savedStateStorage.discard()
        }
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

    // The keeper reads the saved state's outer layer as it is built, so a state it rejects is
    // caught here and the root starts fresh.
    private fun createStateKeeper(savedState: SerializableContainer?): StateKeeperDispatcher =
        runCatching { StateKeeperDispatcher(savedState) }.getOrElse { throwable: Throwable ->
            println("$TAG: the saved state was not readable: $throwable")
            StateKeeperDispatcher()
        }

    private fun createSaveableStateRegistry(
        stateKeeper: StateKeeperDispatcher
    ): SaveableStateRegistry {
        val restored = runCatching {
            stateKeeper.consume(key = SAVEABLE_STATE_KEY, strategy = JsonElement.serializer())
        }.onFailure { throwable: Throwable ->
            println("$TAG: the saved composition state was not readable: $throwable")
        }.getOrNull()
        // Anything a registry hands over is accepted: a stricter check throws inside the
        // composition, and the codec drops what it cannot carry.
        val registry = SaveableStateRegistry(
            restoredValues = restored?.let(SaveableStateCodec::decode),
            canBeSaved = { true }
        )
        stateKeeper.register(key = SAVEABLE_STATE_KEY, strategy = JsonElement.serializer()) {
            SaveableStateCodec.encode(registry.performSave())
        }
        return registry
    }
}
