package com.alekseivinogradov.anoti.main.impl.presentation

import androidx.compose.runtime.mutableStateOf
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.navigation.NavAnimeFavoritesScreenComponent
import com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.navigation.NavAnimeListScreenComponent
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.main.impl.di.DiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.compose.NotificationsRationaleState
import com.alekseivinogradov.anoti.main.impl.presentation.compose.RootDependencies
import com.alekseivinogradov.anoti.main.impl.presentation.navigation.NavRootChild
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionAction
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionRequests
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionStatus
import com.alekseivinogradov.anoti.main.impl.presentation.permission.notificationPermissionAction
import com.alekseivinogradov.anoti.navigation.kmp.NavRootComponent
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy

/**
 * The work every screen host does around the root UI. It builds the root navigation and sets the
 * bottom bar's opening tab. It closes the root stores with the host and runs the notification
 * permission flow. The platform host shows [dependencies] and [notificationsRationale].
 *
 * One instance is one root. A host that rebuilds its root builds a new instance over a new
 * context.
 *
 * @param diRootComponent the graph this root takes its stores and screens from.
 * @param openingTarget the screen a fresh start opens on, such as the one a notification names.
 * Saved state is discarded when it is set. `null` restores the saved screen, or opens the list.
 * @param createComponentContext makes the root's context, discarding saved state when asked.
 * Called exactly once.
 * @param notificationPermissionRequests the platform's ways of getting the permission granted.
 */
internal class RootHost(
    private val diRootComponent: DiRootComponent,
    openingTarget: NavRootConfig?,
    createComponentContext: (discardSavedState: Boolean) -> ComponentContext,
    private val notificationPermissionRequests: NotificationPermissionRequests
) {

    // Both bindings build a new store on every read, so each is read exactly once. The bar's
    // database store is taken before the first screen takes its own.
    private val mainStore: BottomNavigationBarStore = diRootComponent.bottomNavigationBarStore
    private val animeDatabaseStore = diRootComponent.animeDatabaseStore

    private val componentContext: ComponentContext =
        createComponentContext(openingTarget != null)

    private val rationaleVisible = mutableStateOf(false)

    private var onRationaleApproved: () -> Unit = {}

    init {
        // These are closed from where they are created. The binding that would otherwise close
        // them only starts once the first composition's effects run. The host can be gone by
        // then. A second dispose is a no-op, so the binding's own call stays harmless.
        componentContext.lifecycle.doOnDestroy {
            mainStore.dispose()
            animeDatabaseStore.dispose()
        }
    }

    private val rootComponent: NavRootComponent<NavRootChild> = NavRootComponent(
        componentContext = componentContext,
        initialConfiguration = openingTarget ?: NavRootConfig.AnimeList,
        childFactory = ::createRootChild
    )

    init {
        // The only path that gets the bar's first tab right, not a shortcut for one. The view
        // dispatches the same intent, but its binder attaches on a later main-thread message, so
        // that first dispatch reaches no subscriber and is dropped. Removing this leaves the bar
        // highlighting the wrong tab after a launch into favorites. `childStack.value` is
        // already valid here: it resolves the initial or restored child on construction.
        mainStore.accept(
            BottomNavigationBarStore.Intent.ChangeSelectedSection(
                selectedSection = rootComponent.childStack.value.active.instance.section
            )
        )
    }

    /** What the root content draws from. */
    val dependencies = RootDependencies(
        rootComponent = rootComponent,
        mainStore = mainStore,
        animeDatabaseStore = animeDatabaseStore,
        systemMessageController = diRootComponent.parent.systemMessageController,
        lifecycle = componentContext.lifecycle
    )

    /** The notification-permission explanation the root content shows over the screen. */
    val notificationsRationale = NotificationsRationaleState(
        visible = rationaleVisible,
        onDismiss = { rationaleVisible.value = false },
        onApprove = {
            rationaleVisible.value = false
            onRationaleApproved()
        }
    )

    /** Acts on what the platform reported about the notification permission. Main thread only. */
    fun onNotificationPermissionStatus(status: NotificationPermissionStatus) {
        when (notificationPermissionAction(status)) {
            NotificationPermissionAction.NONE -> Unit
            NotificationPermissionAction.PROMPT -> notificationPermissionRequests.prompt()
            NotificationPermissionAction.EXPLAIN_THEN_PROMPT ->
                explainThen(notificationPermissionRequests::prompt)

            NotificationPermissionAction.EXPLAIN_THEN_OPEN_SETTINGS ->
                explainThen(notificationPermissionRequests::openSettings)
        }
    }

    private fun explainThen(request: () -> Unit) {
        onRationaleApproved = request
        rationaleVisible.value = true
    }

    private fun createRootChild(
        config: NavRootConfig,
        componentContext: ComponentContext
    ): NavRootChild =
        when (config) {
            NavRootConfig.AnimeList -> NavRootChild.List(
                NavAnimeListScreenComponent(
                    componentContext = componentContext,
                    diAnimeListComponent = diRootComponent.createDiAnimeListComponent()
                )
            )

            NavRootConfig.AnimeFavorites -> NavRootChild.Favorites(
                NavAnimeFavoritesScreenComponent(
                    componentContext = componentContext,
                    diAnimeFavoritesComponent = diRootComponent.createDiAnimeFavoritesComponent()
                )
            )
        }
}
