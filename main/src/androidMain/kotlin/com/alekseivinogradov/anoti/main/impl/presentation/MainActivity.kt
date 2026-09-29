package com.alekseivinogradov.anoti.main.impl.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.alekseivinogradov.anoti.main.impl.presentation.compose.RootContent
import com.alekseivinogradov.anoti.main.impl.presentation.di.DiRootComponentHolder
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionRequests
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionStatus
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import com.alekseivinogradov.anoti.navigation.kmp.NavRootDeepLink
import com.arkivanov.decompose.defaultComponentContext

/**
 * The Android entry point. Hands the root UI's shared work to a [RootHost], shows the root
 * content, and supplies what only Android can: the saved state, the notification permission and
 * the screen a tapped notification names.
 */
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val notificationPermissionRequests = object : NotificationPermissionRequests {
        override fun prompt() {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        override fun openSettings() {
            val intent = Intent().also {
                it.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                it.putExtra(
                    /* name = */
                    Settings.EXTRA_APP_PACKAGE,
                    /* value = */
                    packageName
                )
            }
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // defaultComponentContext() reads the SavedStateRegistry, which only becomes readable
        // once super.onCreate() has restored it — so it must run first.
        super.onCreate(savedInstanceState)
        // application is assigned in Activity.attach(), before onCreate, so it is never null here.
        // The cast emits its own null check anyway, and that one names the expected type.
        @Suppress("CastNullableToNonNullableType")
        val componentHolder = application as DiRootComponentHolder
        // getIntent() keeps returning the launching Intent for the whole task, so the deep link
        // must only be honored on a fresh start. Otherwise, every Activity recreation would
        // discard the restored navigation state and jump back to the deep link's target.
        val rootHost = RootHost(
            diRootComponent = componentHolder.createDiRootComponent(),
            openingTarget = if (savedInstanceState == null) readDeepLinkTarget() else null,
            createComponentContext = { discardSavedState: Boolean ->
                defaultComponentContext(discardSavedState = discardSavedState)
            },
            notificationPermissionRequests = notificationPermissionRequests
        )

        setSystemSettings()
        setContent {
            RootContent(
                dependencies = rootHost.dependencies,
                notificationsRationale = rootHost.notificationsRationale
            )
        }
        rootHost.onNotificationPermissionStatus(readNotificationPermissionStatus())
    }

    /**
     * This Activity is exported, so any app can launch it with an arbitrary extra — a malformed
     * payload is treated as "no deep link" rather than being allowed to crash [onCreate]. Reading
     * the extra is inside the guard as well: extras that arrive from another process are
     * unpacked on first access, and one naming a class this app doesn't have throws right there.
     */
    private fun readDeepLinkTarget(): NavRootConfig? = NavRootDeepLink.decode(
        payload = runCatching { intent?.getStringExtra(EXTRA_DEEP_LINK_TARGET) }.getOrNull()
    )

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setSystemSettings() {
        // dark() on both edges does two things this app's always-black chrome needs: it forces
        // light bar icons regardless of the system's day/night setting, and it leaves both bars
        // transparent with no contrast scrim — the Compose bottom bar paints its own black
        // behind the system navigation area.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        // The screens are laid out for portrait. The system only honors this below sw 600dp;
        // larger screens rotate freely.
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    // Below Android 13 notifications need no runtime permission, so the system has no question
    // to ask. They can only be switched back on in the settings.
    private fun readNotificationPermissionStatus(): NotificationPermissionStatus =
        if (Build.VERSION.SDK_INT >= TIRAMISU) {
            NotificationPermissionStatus(
                isAllowed = ContextCompat.checkSelfPermission(
                    /* context = */
                    this,
                    /* permission = */
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED,
                canPrompt = true,
                isExplanationOwed = ActivityCompat.shouldShowRequestPermissionRationale(
                    /* activity = */
                    this,
                    /* permission = */
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        } else {
            NotificationPermissionStatus(
                isAllowed = NotificationManagerCompat.from(this).areNotificationsEnabled(),
                canPrompt = false,
                isExplanationOwed = false
            )
        }

    companion object {
        const val EXTRA_DEEP_LINK_TARGET = "deep_link_target"
    }
}
