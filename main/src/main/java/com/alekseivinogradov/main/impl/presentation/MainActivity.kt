package com.alekseivinogradov.main.impl.presentation

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.room.impl.data.AnimeDatabase
import com.alekseivinogradov.database.room.impl.data.repository.AnimeDatabaseRepositoryImpl
import com.alekseivinogradov.main.R
import com.alekseivinogradov.main.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.alekseivinogradov.theme.R as theme_R

class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"

    private val mainActivityScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main +
                CoroutineExceptionHandler { _, e ->
                    Log.e(tag, "$e")
                }
    )

    private var binding: ActivityMainBinding? = null

    val animeDatabase: AnimeDatabase by lazy(LazyThreadSafetyMode.NONE) {
        AnimeDatabase.getDatabase(context = applicationContext)
    }

    val animeDatabaseRepository: AnimeDatabaseRepository by lazy(LazyThreadSafetyMode.NONE) {
        AnimeDatabaseRepositoryImpl(animeDao = animeDatabase.animeDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSystemSettings()
        setBottomNavMenu()
    }

    override fun onDestroy() {
        mainActivityScope.cancel()
        super.onDestroy()
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

    private fun setBottomNavMenu() {
        binding!!.bottomNavMenu.setOnItemSelectedListener { menuItem: MenuItem ->
            val navController = findNavController(R.id.nav_host_fragment)
            when (menuItem.itemId) {
                R.id.anime_list -> {
                    navController.navigate(R.id.anime_list)
                    true
                }

                R.id.anime_favorites -> {
                    navController.navigate(R.id.anime_favorites)
                    true
                }

                else -> false
            }
        }

        val badge = binding!!.bottomNavMenu.getOrCreateBadge(R.id.anime_favorites)
        mainActivityScope.launch {
            animeDatabaseRepository.getAllItemsFlow()
                .map {
                    val newEpisodesOnlyList = it.filter { it.isNewEpisode == true }
                    newEpisodesOnlyList.count()
                }
                .flowOn(Dispatchers.Default)
                .collect { favoritesCount: Int ->
                    if (favoritesCount > 0) {
                        badge.number = favoritesCount
                        badge.isVisible = true
                    } else {
                        badge.isVisible = false
                    }
                }
        }
    }
}
