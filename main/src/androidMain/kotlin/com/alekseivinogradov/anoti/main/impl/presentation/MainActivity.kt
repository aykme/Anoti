package com.alekseivinogradov.anoti.main.impl.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Build.VERSION_CODES.P
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.navigation.NavAnimeFavoritesScreenComponentHolder
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.navigation.NavAnimeFavoritesScreenComponent
import com.alekseivinogradov.anoti.animelist.android.impl.presentation.navigation.NavAnimeListScreenComponentHolder
import com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.navigation.NavAnimeListScreenComponent
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.model.SectionDomain
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation.BottomNavigationBarController
import com.alekseivinogradov.anoti.celebrity.android.impl.presentation.edgetoedge.isEdgeToEdgeEnabled
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.anotiColorScheme
import com.alekseivinogradov.anoti.main.R
import com.alekseivinogradov.anoti.main.impl.di.DiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.di.DiRootComponentHolder
import com.alekseivinogradov.anoti.main.impl.presentation.navigation.NavRootChild
import com.alekseivinogradov.anoti.main.impl.presentation.navigation.NavRootChildFragmentBinder
import com.alekseivinogradov.anoti.navigation.kmp.NavRootComponent
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import com.alekseivinogradov.anoti.notificationsrationaledialog.kmp.api.presentation.compose.NotificationsRationaleDialog
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.essenty.lifecycle.asEssentyLifecycle
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import kotlinx.serialization.json.Json
import com.alekseivinogradov.anoti.celebrity.kmp.R as res_R

class MainActivity :
    AppCompatActivity(),
    NavAnimeListScreenComponentHolder,
    NavAnimeFavoritesScreenComponentHolder {

    private lateinit var diRootComponent: DiRootComponent

    private lateinit var rootComponent: NavRootComponent<NavRootChild>

    override val navAnimeListScreenComponent: NavAnimeListScreenComponent
        get() = (rootComponent.childStack.value.active.instance as NavRootChild.List).component

    override val navAnimeFavoritesScreenComponent: NavAnimeFavoritesScreenComponent
        get() = (rootComponent.childStack.value.active.instance as NavRootChild.Favorites).component

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private var mainLayout: ConstraintLayout? = null

    private lateinit var mainStore: BottomNavigationBarStore

    private lateinit var animeDatabaseStore: AnimeDatabaseStore

    private val notificationsRationaleVisible = mutableStateOf(false)

    private val controller: BottomNavigationBarController by lazy {
        BottomNavigationBarController(
            lifecycle = essentyLifecycle(),
            mainStore = mainStore,
            animeDatabaseStore = animeDatabaseStore
        )
    }

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
        // Set directly on the store (bypassing the view/store event binding, which only
        // completes asynchronously) so the bar's selected tab is already correct for the very
        // first composition, before BottomNavigationBarViewImpl even exists.
        mainStore.accept(
            BottomNavigationBarStore.Intent.ChangeSelectedSection(
                selectedSection = mapNavConfigToSection(initialNavConfig)
            )
        )
        rootComponent = NavRootComponent(
            componentContext = defaultComponentContext(discardSavedState = deepLinkTarget != null),
            initialConfiguration = initialNavConfig,
            childFactory = ::createRootChild
        )

        setContentView(R.layout.activity_main)
        mainLayout = findViewById(R.id.main_layout)
        setSystemSettings()
        // mainLayout is always non-null here: assigned right above, cleared only in onDestroy().
        @Suppress("UnsafeCallOnNullableType")
        val nonNullMainLayout = mainLayout!!
        NavRootChildFragmentBinder(
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_fragment
        ).bind(childStack = rootComponent.childStack, lifecycle = lifecycle.asEssentyLifecycle())
        val bottomNavigationBarView = BottomNavigationBarViewImpl(
            rootView = nonNullMainLayout,
            mainStore = mainStore,
            rootComponent = rootComponent,
            lifecycle = lifecycle.asEssentyLifecycle()
        )
        controller.onViewCreated(
            mainView = bottomNavigationBarView,
            viewLifecycle = lifecycle.asEssentyLifecycle(),
        )
        // Must run after onViewCreated binds this view's events to the store — see
        // BottomNavigationBarViewImpl.startObservingChildStack's own doc.
        bottomNavigationBarView.startObservingChildStack()
        setUpNotificationsRationaleDialog(nonNullMainLayout)
        requestToEnableNotificationsIfNecessary()
    }

    private fun setUpNotificationsRationaleDialog(rootView: View) {
        val dialogHost: ComposeView = rootView.findViewById(R.id.notifications_dialog_host)
        dialogHost.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        dialogHost.setContent {
            MaterialTheme(colorScheme = anotiColorScheme()) {
                if (notificationsRationaleVisible.value) {
                    NotificationsRationaleDialog(
                        onDismiss = { notificationsRationaleVisible.value = false },
                        onApprove = {
                            notificationsRationaleVisible.value = false
                            onNotificationRequestApproved()
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainLayout = null
    }

    private fun mapNavConfigToSection(config: NavRootConfig): SectionDomain =
        when (config) {
            NavRootConfig.AnimeList -> SectionDomain.MAIN
            NavRootConfig.AnimeFavorites -> SectionDomain.FAVORITES
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
                    // Deliberately not systemBars.bottom: BottomNavigationBar's own Spacer
                    // (windowInsetsBottomHeight(WindowInsets.navigationBars)) is the single
                    // consumer of that inset — applying it here too would double-pad the content.
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
