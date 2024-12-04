package com.alekseivinogradov.main.impl.presentation

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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.bottom_navigation_bar.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.bottom_navigation_bar.impl.presentation.BottomNavigationBarController
import com.alekseivinogradov.celebrity.impl.presentation.edge_to_edge.isEdgeToEdgeEnabled
import com.alekseivinogradov.di.api.presentation.app.ApplicationExternal
import com.alekseivinogradov.di.api.presentation.main.MainActivityExternal
import com.alekseivinogradov.di.api.presentation.main.MainComponent
import com.alekseivinogradov.di.api.presentation.scope.ActivityScope
import com.alekseivinogradov.main.R
import com.alekseivinogradov.main.databinding.ActivityMainBinding
import com.alekseivinogradov.main.impl.presentation.di.DaggerMainComponentInternal
import com.arkivanov.essenty.lifecycle.asEssentyLifecycle
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import javax.inject.Inject
import com.alekseivinogradov.res.R as res_R

@ActivityScope
class MainActivity : AppCompatActivity(), MainActivityExternal {

    override lateinit var mainComponent: MainComponent

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private var binding: ActivityMainBinding? = null

    @Inject
    internal lateinit var mainStore: BottomNavigationBarStore

    @Inject
    internal lateinit var animeDatabaseStore: AnimeDatabaseStore

    private val controller: BottomNavigationBarController by lazy {
        BottomNavigationBarController(
            lifecycle = essentyLifecycle(),
            mainStore = mainStore,
            animeDatabaseStore = animeDatabaseStore
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mainComponent = DaggerMainComponentInternal.factory().create(
            activityContext = this as Context,
            appComponent = (this.application as ApplicationExternal).appComponent
        ).also { it.inject(activity = this) }
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSystemSettings()
        controller.onViewCreated(
            mainView = BottomNavigationBarViewImpl(
                viewBinding = binding!!,
                navController = getNavController()
            ),
            viewLifecycle = lifecycle.asEssentyLifecycle(),
        )
        requestToEnableNotificationsIfNecessary()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun getNavController(): NavController {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setSystemSettings() {
        if (isEdgeToEdgeEnabled()) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(
                    Color.TRANSPARENT
                )
            )
            ViewCompat.setOnApplyWindowInsetsListener(binding!!.mainLayout) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(
                    /* left = */systemBars.left,
                    /* top = */0,
                    /* right = */systemBars.right,
                    /** systemBars.bottom works uncorrectly with BottomNavigationView.
                     * It makes double padding and
                     * status bar color elements problems on light theme
                     */
                    /* bottom = */0
                )
                insets
            }
        }

        /**
         * window.setNavigationBarColor() and window.setStatusBarColor() doesn't work correctly
         * with BottomNavigationView on 27 api level or lower.
         * Use android:navigationBarColor and android:statusBarColor from xml instead
         */
        if (Build.VERSION.SDK_INT >= P) {
            /**
             * It's deprecated for api 35 and above, because edge to edge always on.
             * But changing navigation bar color using edge to edge is not working correctry
             * with BottomNavigationView. So this method needed before problem will be fixed
             */
            @Suppress("DEPRECATION")
            window.setNavigationBarColor(getColor(res_R.color.black))
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    }

    private fun requestToEnableNotificationsIfNecessary() {
        if (Build.VERSION.SDK_INT >= TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    /* context = */ this,
                    /* permission = */ Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> Unit

                ActivityCompat.shouldShowRequestPermissionRationale(
                    /* activity = */ this,
                    /* permission = */ Manifest.permission.POST_NOTIFICATIONS
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
        MaterialAlertDialogBuilder(
            /* context = */ this,
            /* overrideThemeResId = */ res_R.style.Theme_Anoti_MaterialAlertDialog
        )
            .setIcon(res_R.mipmap.ic_launcher)
            .setTitle(applicationContext.getString(R.string.dialog_alert_title))
            .setMessage(
                applicationContext.getString(R.string.dialog_alert_notifications_rationale_message)
            )
            .setNegativeButton(
                /* text = */ applicationContext.getString(R.string.dialog_alert_negative_button),
                /* listener = */ null
            )
            .setPositiveButton(
                applicationContext.getString(R.string.dialog_alert_positive_button)
            ) { _, _ -> onNotificationRequestApproved() }
            .show()
    }

    private fun onNotificationRequestApproved() {
        if (Build.VERSION.SDK_INT >= TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            val intent = Intent().also {
                it.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                it.putExtra(
                    /* name = */ Settings.EXTRA_APP_PACKAGE,
                    /* value = */ this.packageName
                )
            }
            this.startActivity(intent)
        }
    }
}
