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
import com.alekseivinogradov.bottom_navigation_bar.impl.presentation.BottomNavigationBarController
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.impl.domain.coroutine_context.CoroutineContextProviderPlatform
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.room.impl.data.AnimeDatabase
import com.alekseivinogradov.database.room.impl.data.repository.AnimeDatabaseRepositoryImpl
import com.alekseivinogradov.main.R
import com.alekseivinogradov.main.databinding.ActivityMainBinding
import com.arkivanov.essenty.lifecycle.asEssentyLifecycle
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.alekseivinogradov.theme.R as theme_R

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    private val coroutineContextProvider: CoroutineContextProvider
            by lazy(LazyThreadSafetyMode.NONE) {
                CoroutineContextProviderPlatform(applicationContext)
            }

    private val animeDatabase: AnimeDatabase by lazy(LazyThreadSafetyMode.NONE) {
        AnimeDatabase.getDatabase(context = applicationContext)
    }

    private val animeDatabaseRepository: AnimeDatabaseRepository by lazy(LazyThreadSafetyMode.NONE) {
        AnimeDatabaseRepositoryImpl(animeDao = animeDatabase.animeDao())
    }

    private val controller: BottomNavigationBarController by lazy {
        BottomNavigationBarController(
            storeFactory = DefaultStoreFactory(),
            lifecycle = essentyLifecycle(),
            coroutineContextProvider = coroutineContextProvider,
            databaseRepository = animeDatabaseRepository
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
