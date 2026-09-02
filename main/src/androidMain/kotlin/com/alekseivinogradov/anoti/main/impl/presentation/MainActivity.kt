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
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.navigation.NavAnimeFavoritesScreenComponent
import com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.navigation.NavAnimeListScreenComponent
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.main.impl.di.DiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.compose.NotificationsRationaleState
import com.alekseivinogradov.anoti.main.impl.presentation.compose.RootContent
import com.alekseivinogradov.anoti.main.impl.presentation.compose.RootDependencies
import com.alekseivinogradov.anoti.main.impl.presentation.di.DiRootComponentHolder
import com.alekseivinogradov.anoti.main.impl.presentation.navigation.NavRootChild
import com.alekseivinogradov.anoti.navigation.kmp.NavRootComponent
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.essenty.lifecycle.asEssentyLifecycle
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {

    private lateinit var diRootComponent: DiRootComponent

    private lateinit var rootComponent: NavRootComponent<NavRootChild>

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private lateinit var mainStore: BottomNavigationBarStore

    private lateinit var animeDatabaseStore: AnimeDatabaseStore

    private val notificationsRationaleVisible = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        // defaultComponentContext() reads the SavedStateRegistry, which only becomes readable
        // once super.onCreate() has restored it — so it must run first.
        super.onCreate(savedInstanceState)
        diRootComponent = (this.application as DiRootComponentHolder).createDiRootComponent()
        mainStore = diRootComponent.bottomNavigationBarStore
        animeDatabaseStore = diRootComponent.animeDatabaseStore
        // getIntent() keeps returning the launching Intent for the whole task, so the deep link
        // must only be honored on a fresh start. Otherwise, every Activity recreation would
        // discard the restored navigation state and jump back to the deep link's target.
        val deepLinkTarget = if (savedInstanceState == null) readDeepLinkTarget() else null
        val initialNavConfig = deepLinkTarget ?: NavRootConfig.AnimeList
        rootComponent = NavRootComponent(
            componentContext = defaultComponentContext(discardSavedState = deepLinkTarget != null),
            initialConfiguration = initialNavConfig,
            childFactory = ::createRootChild
        )
        // Set directly on the store (bypassing the view/store event binding, which only
        // completes asynchronously) so the bar's selected tab is already correct for the very
        // first composition, before RootContent even exists. childStack.value is already valid
        // here: childStack() resolves the initial/restored child synchronously on construction.
        mainStore.accept(
            BottomNavigationBarStore.Intent.ChangeSelectedSection(
                selectedSection = rootComponent.childStack.value.active.instance.section
            )
        )

        setSystemSettings()
        setContent {
            RootContent(
                dependencies = RootDependencies(
                    rootComponent = rootComponent,
                    mainStore = mainStore,
                    animeDatabaseStore = animeDatabaseStore,
                    lifecycle = lifecycle.asEssentyLifecycle()
                ),
                notificationsRationale = NotificationsRationaleState(
                    visible = notificationsRationaleVisible,
                    onDismiss = { notificationsRationaleVisible.value = false },
                    onApprove = {
                        notificationsRationaleVisible.value = false
                        onNotificationRequestApproved()
                    }
                )
            )
        }
        requestToEnableNotificationsIfNecessary()
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

    /**
     * This Activity is exported, so any app can launch it with an arbitrary extra — a malformed
     * payload is treated as "no deep link" rather than being allowed to crash [onCreate].
     */
    private fun readDeepLinkTarget(): NavRootConfig? {
        val encoded = intent?.getStringExtra(EXTRA_DEEP_LINK_TARGET) ?: return null
        return runCatching {
            Json.decodeFromString(NavRootConfig.serializer(), encoded)
        }.getOrNull()
    }

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
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun requestToEnableNotificationsIfNecessary() {
        if (Build.VERSION.SDK_INT >= TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    /* context = */
                    this,
                    /* permission = */
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> Unit

                ActivityCompat.shouldShowRequestPermissionRationale(
                    /* activity = */
                    this,
                    /* permission = */
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    showNotificationsRationale()
                }

                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            if (NotificationManagerCompat.from(this).areNotificationsEnabled().not()) {
                showNotificationsRationale()
            }
        }
    }

    private fun showNotificationsRationale() {
        notificationsRationaleVisible.value = true
    }

    private fun onNotificationRequestApproved() {
        if (Build.VERSION.SDK_INT >= TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            val intent = Intent().also {
                it.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                it.putExtra(
                    /* name = */
                    Settings.EXTRA_APP_PACKAGE,
                    /* value = */
                    this.packageName
                )
            }
            this.startActivity(intent)
        }
    }

    companion object {
        const val EXTRA_DEEP_LINK_TARGET = "deep_link_target"
    }
}
