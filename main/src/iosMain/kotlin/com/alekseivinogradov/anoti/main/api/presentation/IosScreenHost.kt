package com.alekseivinogradov.anoti.main.api.presentation

import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import com.alekseivinogradov.anoti.main.api.di.DiRootDependencies
import com.alekseivinogradov.anoti.main.impl.di.createDiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.RootLifecycle
import com.alekseivinogradov.anoti.main.impl.presentation.RootSession
import com.alekseivinogradov.anoti.main.impl.presentation.compose.RootSessionContent
import com.alekseivinogradov.anoti.main.impl.presentation.notification.NotificationTapDelegate
import com.alekseivinogradov.anoti.main.impl.presentation.permission.IosNotificationPermissionRequests
import com.alekseivinogradov.anoti.main.impl.presentation.permission.readIosNotificationPermissionStatus
import com.alekseivinogradov.anoti.main.impl.presentation.savedstate.SavedStateFile
import com.alekseivinogradov.anoti.main.impl.presentation.savedstate.SavedStateStorage
import com.arkivanov.essenty.lifecycle.ApplicationLifecycle
import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIScene
import platform.UIKit.UIViewController
import platform.UserNotifications.UNUserNotificationCenter

private const val TAG = "IosScreenHost"
private const val SAVED_STATE_DIRECTORY = "saved_state"
private const val SAVED_STATE_FILE = "root_saved_state.json"

/**
 * The iOS screen host, the counterpart of `MainActivity`. It shows the app's screen and supplies
 * what only iOS can: the lifecycle, the notification permission, the screen a tapped
 * notification names and a place to keep the screen's state. One instance lives for the whole
 * process, and each composition of its screen gets a new root.
 *
 * Main thread only.
 *
 * @param diRootDependencies the app-wide graph every root's graph is built on.
 */
class IosScreenHost(diRootDependencies: DiRootDependencies) {

    private val session = RootSession(
        createDiRootComponent = { createDiRootComponent(parent = diRootDependencies) },
        createLifecycle = ::createRootLifecycle,
        notificationPermissionRequests = IosNotificationPermissionRequests(),
        readNotificationPermissionStatus = ::readIosNotificationPermissionStatus,
        savedStateStorage = SavedStateStorage(
            file = SavedStateFile(
                fileSystem = FileSystem.SYSTEM,
                path = "${savedStateDirectory()}/$SAVED_STATE_FILE".toPath()
            ),
            appVersion = appVersion(),
            sceneSessionId = ::connectedSceneSessionId
        )
    )

    // Held here: the notification center keeps only a weak reference to its delegate.
    private val notificationTapDelegate = NotificationTapDelegate(
        openFromNotification = session::openFromNotification
    )

    /**
     * Starts what the host serves for the whole process. Called once, before the app finishes
     * launching, so a tap that launched the app reaches the delegate.
     */
    fun start() {
        UNUserNotificationCenter.currentNotificationCenter().delegate = notificationTapDelegate
        // The state is written inside the callback, so the write ends before the app is
        // suspended. UIKit posts it whether or not the app uses scenes.
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidEnterBackgroundNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { _ ->
            session.saveState()
        }
    }

    /** Builds a new view controller over the app's screen. */
    fun viewController(): UIViewController = ComposeUIViewController(
        configure = {
            // The screen keeps the focused field above the keyboard itself, as on Android.
            // The default would pan the whole view on top of that.
            onFocusBehavior = OnFocusBehavior.DoNothing
        }
    ) {
        RootSessionContent(session = session)
    }
}

// It follows the whole app, since the screen is the app's only one. The root's end destroys it,
// which also drops its observers.
private fun createRootLifecycle(): RootLifecycle {
    val lifecycle = ApplicationLifecycle()
    return RootLifecycle(lifecycle = lifecycle, end = lifecycle::destroy)
}

// Application Support survives until the app is deleted, unlike Caches. The directory is kept
// out of backups: a restored copy would carry another session and never be accepted.
@OptIn(ExperimentalForeignApi::class)
private fun savedStateDirectory(): String {
    val fileManager = NSFileManager.defaultManager
    val applicationSupport = checkNotNull(
        fileManager.URLForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )
    ) { "Application Support is not available" }
    val directory: NSURL = checkNotNull(
        applicationSupport.URLByAppendingPathComponent(SAVED_STATE_DIRECTORY)
    )
    val path = checkNotNull(directory.path)
    if (!fileManager.fileExistsAtPath(path)) {
        val created = fileManager.createDirectoryAtURL(
            url = directory,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
        val excluded = directory.setResourceValue(
            value = true,
            forKey = NSURLIsExcludedFromBackupKey,
            error = null
        )
        println("$TAG: saved-state directory created: $created, kept out of backups: $excluded")
    }
    return path
}

private fun appVersion(): String {
    val bundle = NSBundle.mainBundle
    val name = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString")
    val build = bundle.objectForInfoDictionaryKey("CFBundleVersion")
    return "$name ($build)"
}

// The app has one scene. The states are logged to learn how a launch really starts.
private fun connectedSceneSessionId(): String? {
    val application = UIApplication.sharedApplication
    val scene = application.connectedScenes.firstOrNull() as? UIScene
    println(
        "$TAG: app state ${application.applicationState}, " +
            "scene state ${scene?.activationState}"
    )
    return scene?.session?.persistentIdentifier
}
