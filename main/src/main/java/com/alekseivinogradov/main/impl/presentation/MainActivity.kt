package com.alekseivinogradov.main.impl.presentation

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
import com.alekseivinogradov.theme.R as theme_R

class MainActivity : AppCompatActivity() {

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

    private val databaseStore: DatabaseStore by lazy(LazyThreadSafetyMode.NONE) {
        DatabaseStoreFactory(
            storeFactory = storeFactory,
            coroutineContextProvider = coroutineContextProvider,
            usecases = databaseUsecases
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
    }

    private fun getNavController(): NavController {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setSystemSettings() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding!!.mainLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                /* left = */ systemBars.left,
                /* top = */systemBars.top,
                /* right = */systemBars.right,
                /* bottom = */0
            )
            insets
        }

        window.setStatusBarColor(getColor(theme_R.color.black))
        window.setNavigationBarColor(getColor(theme_R.color.black))
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    }
}
