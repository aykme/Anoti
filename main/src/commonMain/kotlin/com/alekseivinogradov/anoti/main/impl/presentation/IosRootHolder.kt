package com.alekseivinogradov.anoti.main.impl.presentation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.lifecycle.ChildLifecycle
import com.alekseivinogradov.anoti.main.impl.di.DiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionRequests
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionStatus
import com.alekseivinogradov.anoti.main.impl.presentation.savedstate.SaveableStateCodec
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject

private const val TAG = "IosRootHolder"
private const val APP_VERSION_KEY = "appVersion"
private const val STATE_KEEPER_KEY = "stateKeeper"
private const val SAVEABLE_KEY = "saveable"

/**
 * The iOS app's one root and the string the scene keeps its state in. iOS has no saved-instance
 * state of its own, so this holder writes the root's state and the screen's `rememberSaveable`
 * values into one string and reads them back.
 *
 * The root is built on the first [rootFor] and lives as long as the app. Each composition of it
 * gets its own saveable-state registry, carried over from the one before.
 *
 * Main thread only. Compose reads nothing from it that changes.
 *
 * @param createDiRootComponent builds the root's graph.
 * @param appLifecycle the app's lifecycle, which the root follows.
 * @param appVersion the build writing a state. A state another build wrote is dropped.
 * @param notificationPermissionRequests the platform's ways of getting the permission granted.
 * @param readNotificationPermissionStatus reads what the platform reports about the permission.
 */
@Stable
internal class IosRootHolder(
    private val createDiRootComponent: () -> DiRootComponent,
    private val appLifecycle: Lifecycle,
    private val appVersion: String,
    private val notificationPermissionRequests: NotificationPermissionRequests,
    private val readNotificationPermissionStatus: suspend () -> NotificationPermissionStatus
) {

    private var root: Root? = null

    // The screen a tap asked for before the root was built.
    private var pendingTarget: NavRootConfig? = null

    // What the screen held with rememberSaveable when its last composition ended.
    private var saveableSnapshot: Map<String, List<Any?>>? = null

    // The registry of the composition showing the root, while there is one.
    private var liveRegistry: SaveableStateRegistry? = null

    /**
     * The app's root. The first call builds it over [restoredState], the string the scene kept;
     * `null` or empty means nothing was kept. Later calls return the same root and ignore theirs.
     */
    fun rootFor(restoredState: String?): RootHost {
        root?.let { built: Root ->
            println("$TAG: the root exists, its state is not read again")
            return built.host
        }
        val target = pendingTarget
        pendingTarget = null
        val restored = readState(restoredState)?.let(::buildOver)
        val built = restored ?: build(openingTarget = target, container = null)
        if (restored != null && target != null) {
            restored.host.openFromNotification(target)
        }
        root = built
        checkNotificationPermission(built)
        println("$TAG: the root opens on ${built.host.activeScreen}")
        return built.host
    }

    /** Opens [target] in the root, or keeps it for the root still to be built. */
    fun openFromNotification(target: NavRootConfig) {
        val built = root
        if (built != null) {
            built.host.openFromNotification(target)
        } else {
            pendingTarget = target
        }
        println("$TAG: a notification opens $target, the root exists: ${built != null}")
    }

    /**
     * The string for the scene to keep: the app version, the root's state and what the screen
     * holds with `rememberSaveable`. `null` before the root exists, so the scene keeps what it
     * has. Empty when the state cannot be written, so no older one comes back.
     */
    fun saveState(): String? {
        val built = root ?: run {
            println("$TAG: nothing saved, there is no root")
            return null
        }
        return runCatching {
            val saveable = liveRegistry?.performSave() ?: saveableSnapshot.orEmpty()
            JsonObject(
                mapOf(
                    APP_VERSION_KEY to JsonPrimitive(appVersion),
                    STATE_KEEPER_KEY to Json.encodeToJsonElement(
                        SerializableContainer.serializer(),
                        built.stateKeeper.save()
                    ),
                    SAVEABLE_KEY to SaveableStateCodec.encode(saveable)
                )
            ).toString()
        }.onSuccess { saved: String ->
            println("$TAG: saved ${saved.length} characters on ${built.host.activeScreen}")
        }.getOrElse { throwable: Throwable ->
            println("$TAG: the state was not saved, the kept one is cleared: $throwable")
            ""
        }
    }

    /** A registry for a new composition, holding what the live or the last one held. */
    // Anything a registry hands over is accepted: a stricter check throws inside the
    // composition, and the codec drops what it cannot carry.
    fun newSaveableStateRegistry(): SaveableStateRegistry = SaveableStateRegistry(
        restoredValues = liveRegistry?.performSave() ?: saveableSnapshot,
        canBeSaved = { true }
    )

    /** Makes [registry] the one [saveState] reads, once its composition is shown. */
    fun attach(registry: SaveableStateRegistry) {
        liveRegistry = registry
    }

    /**
     * Keeps what [registry] holds once its composition ends. A registry no longer live changes
     * nothing.
     */
    fun detach(registry: SaveableStateRegistry) {
        if (liveRegistry !== registry) return
        saveableSnapshot = registry.performSave()
        liveRegistry = null
    }

    // A state another build wrote, or one that does not parse, gives a fresh root.
    private fun readState(restoredState: String?): JsonObject? {
        val state = restoredState?.takeUnless { it.isEmpty() }?.let { kept: String ->
            runCatching { Json.parseToJsonElement(kept).jsonObject }
                .onFailure { println("$TAG: the kept state does not parse: $it") }
                .getOrNull()
        }
        val version = (state?.get(APP_VERSION_KEY) as? JsonPrimitive)?.contentOrNull
        return when {
            restoredState.isNullOrEmpty() -> null.also { println("$TAG: nothing was kept") }
            state == null -> null
            version != appVersion -> null.also {
                println("$TAG: the kept state is from $version, not $appVersion, and is dropped")
            }

            else -> state
        }
    }

    // The screens read their parts while the root is built, so a state they reject throws there.
    // The attempt is then closed and the caller builds a fresh root.
    private fun buildOver(state: JsonObject): Root? = runCatching {
        val container = Json.decodeFromJsonElement(
            SerializableContainer.serializer(),
            state.getValue(STATE_KEEPER_KEY)
        )
        build(openingTarget = null, container = container).also {
            saveableSnapshot = SaveableStateCodec.decode(state[SAVEABLE_KEY] ?: JsonNull)
        }
    }.onFailure { throwable: Throwable ->
        println("$TAG: the kept state broke the root, starting fresh: $throwable")
    }.getOrNull()

    private fun build(openingTarget: NavRootConfig?, container: SerializableContainer?): Root {
        val lifecycle = ChildLifecycle(parent = appLifecycle)
        return runCatching {
            val stateKeeper = StateKeeperDispatcher(savedState = container)
            val host = RootHost(
                diRootComponent = createDiRootComponent(),
                openingTarget = openingTarget,
                // A root opening on a target is built over no saved state at all.
                createComponentContext = { _: Boolean ->
                    DefaultComponentContext(lifecycle = lifecycle, stateKeeper = stateKeeper)
                },
                notificationPermissionRequests = notificationPermissionRequests
            )
            Root(host = host, stateKeeper = stateKeeper, lifecycle = lifecycle)
        }.onFailure {
            // Closes whatever the attempt built before it failed.
            lifecycle.destroy()
        }.getOrThrow()
    }

    private fun checkNotificationPermission(root: Root) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        root.lifecycle.doOnDestroy { scope.cancel() }
        scope.launch {
            root.host.onNotificationPermissionStatus(readNotificationPermissionStatus())
        }
    }

    private val RootHost.activeScreen: NavRootConfig
        get() = dependencies.rootComponent.childStack.value.active.configuration

    private class Root(
        val host: RootHost,
        val stateKeeper: StateKeeperDispatcher,
        val lifecycle: ChildLifecycle
    )
}
