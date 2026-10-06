package com.alekseivinogradov.anoti.di.kmp

import com.alekseivinogradov.anoti.main.api.presentation.IosScreenHost
import com.alekseivinogradov.anoti.main.api.presentation.iosSupportedInterfaceOrientations
import platform.UIKit.UIInterfaceOrientationMask
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow

/**
 * The iOS app's entry point, and the only thing its Swift code calls. It builds the app graph and
 * the one screen host, and starts what runs once per process. The Android counterpart is
 * `AnotiApp`.
 *
 * Main thread only, like every UIKit call it serves.
 */
object IosApp {

    private lateinit var screenHost: IosScreenHost

    /**
     * Builds the app. Called from `application(_:didFinishLaunchingWithOptions:)`: the platform
     * wants the background task registered and the notification delegate set before launch
     * ends. A second call does nothing.
     */
    fun start() {
        if (::screenHost.isInitialized) return

        val diAppComponent = createDiAppComponent(appContext = IosAppContext)
        // Building the scheduler registers its background-task handler.
        diAppComponent.animeBackgroundScheduler.schedulePeriodicUpdate()
        screenHost = IosScreenHost(diRootDependencies = diAppComponent).also { it.start() }
    }

    /**
     * A view controller over the app's screen. Called from `makeUIViewController` only, with the
     * string the scene's `@SceneStorage` holds; empty means nothing was kept. Fails when [start]
     * has not run.
     */
    fun viewController(restoredState: String?): UIViewController =
        screenHost.viewController(restoredState)

    /**
     * The string for the scene's `@SceneStorage`, asked for whenever the scene leaves the active
     * phase. `null` means keep the stored one.
     */
    fun saveState(): String? = screenHost.saveState()

    /**
     * The orientations [window] may take, for `application(_:supportedInterfaceOrientationsFor:)`.
     * A narrow window keeps the app upright, as Android keeps a narrow screen upright. Safe to call
     * before [start].
     */
    fun supportedInterfaceOrientations(window: UIWindow?): UIInterfaceOrientationMask =
        iosSupportedInterfaceOrientations(window)
}
