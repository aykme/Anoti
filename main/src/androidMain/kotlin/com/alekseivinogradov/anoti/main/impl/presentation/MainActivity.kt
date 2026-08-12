package com.alekseivinogradov.anoti.main.impl.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Build.VERSION_CODES.P
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.provider.Settings
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.di.AnimeFavoritesScreenComponent
import com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.di.AnimeFavoritesScreenComponentHolder
import com.alekseivinogradov.anoti.animelist.android.impl.presentation.di.AnimeListScreenComponent
import com.alekseivinogradov.anoti.animelist.android.impl.presentation.di.AnimeListScreenComponentHolder
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation.BottomNavigationBarController
import com.alekseivinogradov.anoti.celebrity.android.impl.presentation.edgetoedge.isEdgeToEdgeEnabled
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.main.R
import com.alekseivinogradov.anoti.main.generated.resources.Res
import com.alekseivinogradov.anoti.main.generated.resources.dialog_alert_negative_button
import com.alekseivinogradov.anoti.main.generated.resources.dialog_alert_notifications_rationale_message
import com.alekseivinogradov.anoti.main.generated.resources.dialog_alert_positive_button
import com.alekseivinogradov.anoti.main.generated.resources.dialog_alert_title
import com.alekseivinogradov.anoti.main.impl.presentation.di.MainComponent
import com.alekseivinogradov.anoti.main.impl.presentation.di.MainComponentFactoryHolder
import com.alekseivinogradov.anoti.main.impl.presentation.navigation.RootChild
import com.alekseivinogradov.anoti.main.impl.presentation.navigation.RootChildFragmentBinder
import com.alekseivinogradov.anoti.navigation.kmp.RootComponent
import com.alekseivinogradov.anoti.navigation.kmp.RootConfig
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.essenty.lifecycle.asEssentyLifecycle
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString
import com.alekseivinogradov.anoti.celebrity.kmp.R as res_R

class MainActivity :
    AppCompatActivity(),
    AnimeListScreenComponentHolder,
    AnimeFavoritesScreenComponentHolder {

    private lateinit var mainComponent: MainComponent

    private lateinit var rootComponent: RootComponent<RootChild>

    override val animeListScreenComponent: AnimeListScreenComponent
        get() = (rootComponent.childStack.value.active.instance as RootChild.List).component

    override val animeFavoritesScreenComponent: AnimeFavoritesScreenComponent
        get() = (rootComponent.childStack.value.active.instance as RootChild.Favorites).component

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private var mainLayout: ConstraintLayout? = null

    private lateinit var mainStore: BottomNavigationBarStore

    private lateinit var animeDatabaseStore: AnimeDatabaseStore

    private lateinit var coroutineContextProvider: CoroutineContextProvider

    private val controller: BottomNavigationBarController by lazy {
        BottomNavigationBarController(
            lifecycle = essentyLifecycle(),
            mainStore = mainStore,
            animeDatabaseStore = animeDatabaseStore
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mainComponent = (this.application as MainComponentFactoryHolder)
            .mainComponentFactory
            .createMainComponent(activityContext = this as Context)
        mainStore = mainComponent.bottomNavigationBarStore
        animeDatabaseStore = mainComponent.animeDatabaseStore
        coroutineContextProvider = mainComponent.coroutineContextProvider
        val deepLinkTarget = readDeepLinkTarget()
        rootComponent = RootComponent(
            componentContext = defaultComponentContext(discardSavedState = deepLinkTarget != null),
            initialConfiguration = deepLinkTarget ?: RootConfig.AnimeList,
            childFactory = ::createRootChild
        )
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        mainLayout = findViewById(R.id.main_layout)
        setSystemSettings()
        // mainLayout is always non-null here: assigned right above, cleared only in onDestroy().
        @Suppress("UnsafeCallOnNullableType")
        val nonNullMainLayout = mainLayout!!
        RootChildFragmentBinder(
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_fragment
        ).bind(childStack = rootComponent.childStack, lifecycle = lifecycle.asEssentyLifecycle())
        controller.onViewCreated(
            mainView = BottomNavigationBarViewImpl(
                rootView = nonNullMainLayout,
                rootComponent = rootComponent,
                lifecycle = lifecycle.asEssentyLifecycle()
            ),
            viewLifecycle = lifecycle.asEssentyLifecycle(),
        )
        requestToEnableNotificationsIfNecessary()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainLayout = null
    }

    private fun createRootChild(config: RootConfig, componentContext: ComponentContext): RootChild =
        when (config) {
            RootConfig.AnimeList -> RootChild.List(
                AnimeListScreenComponent(
                    componentContext = componentContext,
                    animeListComponent = mainComponent.animeListComponentFactory.createAnimeListComponent()
                )
            )

            RootConfig.AnimeFavorites -> RootChild.Favorites(
                AnimeFavoritesScreenComponent(
                    componentContext = componentContext,
                    animeFavoritesComponent = mainComponent.animeFavoritesComponentFactory
                        .createAnimeFavoritesComponent()
                )
            )
        }

    private fun readDeepLinkTarget(): RootConfig? {
        val encoded = intent?.getStringExtra(EXTRA_DEEP_LINK_TARGET) ?: return null
        return Json.decodeFromString(RootConfig.serializer(), encoded)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setSystemSettings() {
        if (isEdgeToEdgeEnabled()) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(
                    Color.TRANSPARENT
                )
            )
            // mainLayout is always non-null here: assigned in onCreate() before this is called,
            // cleared only in onDestroy().
            @Suppress("UnsafeCallOnNullableType")
            val nonNullMainLayout = mainLayout!!
            ViewCompat.setOnApplyWindowInsetsListener(nonNullMainLayout) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(
                    /* left = */
                    systemBars.left,
                    /* top = */
                    0,
                    /* right = */
                    systemBars.right,
                    /** systemBars.bottom works incorrectly with BottomNavigationView.
                     * It makes double padding and
                     * status bar color elements problems on light theme
                     */
                    /* bottom = */
                    0
                )
                insets
            }
        }

        /**
         * window.setNavigationBarColor() and window.setStatusBarColor() doesn't work correctly
         * with BottomNavigationView on 27 api level or lower.
         * Use android:navigationBarColor and android:statusBarColor from XML instead
         */
        if (Build.VERSION.SDK_INT >= P) {
            /**
             * It's deprecated for api 35 and above, because edge to edge always on.
             * But changing navigation bar color using edge to edge is not working correctly
             * with BottomNavigationView. So this method needed before problem will be fixed
             */
            @Suppress("DEPRECATION")
            window.navigationBarColor = getColor(res_R.color.black)
        }

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
        val dialogTitle = runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(Res.string.dialog_alert_title)
        }
        val dialogNegativeButton = runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(Res.string.dialog_alert_negative_button)
        }
        val dialogPositiveButton = runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(Res.string.dialog_alert_positive_button)
        }
        val dialogNotificationsRationaleMessage =
            runBlocking(coroutineContextProvider.ioDispatcher) {
                getString(Res.string.dialog_alert_notifications_rationale_message)
            }

        MaterialAlertDialogBuilder(
            /* context = */
            this,
            /* overrideThemeResId = */
            res_R.style.Theme_Anoti_MaterialAlertDialog
        )
            .setIcon(res_R.mipmap.ic_launcher)
            .setTitle(dialogTitle)
            .setMessage(dialogNotificationsRationaleMessage)
            .setNegativeButton(
                /* text = */
                dialogNegativeButton,
                /* listener = */
                null
            )
            .setPositiveButton(dialogPositiveButton) { _, _ -> onNotificationRequestApproved() }
            .show()
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
