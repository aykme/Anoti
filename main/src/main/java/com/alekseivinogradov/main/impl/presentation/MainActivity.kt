package com.alekseivinogradov.main.impl.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Build.VERSION_CODES.P
import android.os.Build.VERSION_CODES.Q
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
import com.alekseivinogradov.bottom_navigation_bar.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.bottom_navigation_bar.impl.domain.store.BottomNavigationBarStoreFactory
import com.alekseivinogradov.bottom_navigation_bar.impl.presentation.BottomNavigationBarController
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.impl.domain.coroutine_context.CoroutineContextProviderPlatform
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.api.domain.store.DatabaseStore
import com.alekseivinogradov.database.api.domain.usecase.ChangeDatabaseItemNewEpisodeStatusUsecase
import com.alekseivinogradov.database.api.domain.usecase.DeleteDatabaseItemUsecase
import com.alekseivinogradov.database.api.domain.usecase.FetchAllDatabaseItemsFlowUsecase
import com.alekseivinogradov.database.api.domain.usecase.InsertDatabaseItemUsecase
import com.alekseivinogradov.database.api.domain.usecase.ResetAllDatabaseItemsNewEpisodeStatusUsecase
import com.alekseivinogradov.database.api.domain.usecase.UpdateDatabaseItemUsecase
import com.alekseivinogradov.database.api.domain.usecase.wrapper.DatabaseUsecases
import com.alekseivinogradov.database.impl.domain.store.DatabaseExecutorImpl
import com.alekseivinogradov.database.impl.domain.store.DatabaseStoreFactory
import com.alekseivinogradov.database.impl.domain.usecase.ChangeDatabaseItemNewEpisodeStatusUsecaseImpl
import com.alekseivinogradov.database.impl.domain.usecase.DeleteDatabaseItemUsecaseImpl
import com.alekseivinogradov.database.impl.domain.usecase.FetchAllDatabaseItemsFlowUsecaseImpl
import com.alekseivinogradov.database.impl.domain.usecase.InsertDatabaseItemUsecaseImpl
import com.alekseivinogradov.database.impl.domain.usecase.ResetAllDatabaseItemsNewEpisodeStatusUsecaseImpl
import com.alekseivinogradov.database.impl.domain.usecase.UpdateDatabaseItemUsecaseImpl
import com.alekseivinogradov.database.room.impl.data.AnimeDatabase
import com.alekseivinogradov.database.room.impl.data.repository.AnimeDatabaseRepositoryImpl
import com.alekseivinogradov.main.R
import com.alekseivinogradov.main.databinding.ActivityMainBinding
import com.arkivanov.essenty.lifecycle.asEssentyLifecycle
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.alekseivinogradov.res.R as res_R

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private var binding: ActivityMainBinding? = null

    private val storeFactory: StoreFactory = DefaultStoreFactory()

    private val coroutineContextProvider: CoroutineContextProvider by lazy {
        CoroutineContextProviderPlatform(applicationContext)
    }

    private val animeDatabase: AnimeDatabase by lazy(LazyThreadSafetyMode.NONE) {
        AnimeDatabase.getDatabase(appContext = applicationContext)
    }

    private val animeDatabaseRepository: AnimeDatabaseRepository by lazy(LazyThreadSafetyMode.NONE) {
        AnimeDatabaseRepositoryImpl(animeDao = animeDatabase.animeDao())
    }

    private val fetchAllDatabaseItemsFlowUsecase: FetchAllDatabaseItemsFlowUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                FetchAllDatabaseItemsFlowUsecaseImpl(repository = animeDatabaseRepository)
            }

    private val insertDatabaseItemUsecase: InsertDatabaseItemUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                InsertDatabaseItemUsecaseImpl(repository = animeDatabaseRepository)
            }

    private val deleteDatabaseItemUsecase: DeleteDatabaseItemUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                DeleteDatabaseItemUsecaseImpl(repository = animeDatabaseRepository)
            }

    private val resetAllDatabaseItemsNewEpisodeStatusUsecase
            : ResetAllDatabaseItemsNewEpisodeStatusUsecase by lazy(LazyThreadSafetyMode.NONE) {
        ResetAllDatabaseItemsNewEpisodeStatusUsecaseImpl(repository = animeDatabaseRepository)
    }

    private val changeDatabaseItemNewEpisodeStatusUsecase
            : ChangeDatabaseItemNewEpisodeStatusUsecase by lazy(LazyThreadSafetyMode.NONE) {
        ChangeDatabaseItemNewEpisodeStatusUsecaseImpl(repository = animeDatabaseRepository)
    }

    private val updateDatabaseItemUsecase: UpdateDatabaseItemUsecase
            by lazy(LazyThreadSafetyMode.NONE) {
                UpdateDatabaseItemUsecaseImpl(repository = animeDatabaseRepository)
            }

    private val databaseUsecases by lazy(LazyThreadSafetyMode.NONE) {
        DatabaseUsecases(
            fetchAllDatabaseItemsFlowUsecase = fetchAllDatabaseItemsFlowUsecase,
            insertDatabaseItemUsecase = insertDatabaseItemUsecase,
            deleteDatabaseItemUsecase = deleteDatabaseItemUsecase,
            resetAllDatabaseItemsNewEpisodeStatusUsecase =
            resetAllDatabaseItemsNewEpisodeStatusUsecase,
            changeDatabaseItemNewEpisodeStatusUsecase = changeDatabaseItemNewEpisodeStatusUsecase,
            updateDatabaseItemUsecase = updateDatabaseItemUsecase
        )
    }

    private val mainStore: BottomNavigationBarStore = BottomNavigationBarStoreFactory(
        storeFactory = storeFactory
    ).create()

    private val databaseExecutorFactory: () -> DatabaseExecutorImpl
            by lazy(LazyThreadSafetyMode.NONE) { createDatabaseExecutorFactory() }

    private val databaseStore: DatabaseStore by lazy(LazyThreadSafetyMode.NONE) {
        DatabaseStoreFactory(
            storeFactory = storeFactory,
            executorFactory = databaseExecutorFactory
        ).create()
    }

    private val controller: BottomNavigationBarController by lazy {
        BottomNavigationBarController(
            lifecycle = essentyLifecycle(),
            mainStore = mainStore,
            databaseStore = databaseStore
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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

    private fun getNavController(): NavController {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setSystemSettings() {
        /**
         * enableEdgeToEdge() doesn't work correctly with BottomNavigationView
         * and window.navigationBarColor() or android:statusBarColor (xml)
         * on 28 api level or lower.
         * There is a bug with navigation bar elements color on light theme.
         */
        if (Build.VERSION.SDK_INT >= Q) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(
                    Color.TRANSPARENT
                )
            )
            ViewCompat.setOnApplyWindowInsetsListener(binding!!.mainLayout) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(
                    /* left = */ systemBars.left,
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

    private fun createDatabaseExecutorFactory(): () -> DatabaseExecutorImpl = {
        DatabaseExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = databaseUsecases
        )
    }
}
