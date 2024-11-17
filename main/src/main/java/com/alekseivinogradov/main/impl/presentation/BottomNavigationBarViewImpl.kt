package com.alekseivinogradov.main.impl.presentation

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.alekseivinogradov.bottom_navigation_bar.api.domain.model.SectionDomain
import com.alekseivinogradov.bottom_navigation_bar.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.bottom_navigation_bar.api.presentation.model.UiModel
import com.alekseivinogradov.bottom_navigation_bar.impl.presentation.BottomNavigationBarView
import com.alekseivinogradov.main.R
import com.alekseivinogradov.main.databinding.ActivityMainBinding
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import com.google.android.material.badge.BadgeDrawable
import com.alekseivinogradov.res.R as res_R

internal class BottomNavigationBarViewImpl(
    private val viewBinding: ActivityMainBinding,
    private val navController: NavController
) : BottomNavigationBarView, BaseMviView<UiModel, BottomNavigationBarStore.Intent>() {

    private val context: Context
        get() = viewBinding.root.context

    private val favoritesBadge: BadgeDrawable =
        viewBinding.bottomNavMenu.getOrCreateBadge(R.id.anime_favorites)

    init {
        initOnItemSelectedListener()
        initOnDestinationChangeListener()
        initFavoritesBadge()
    }

    override val renderer: ViewRenderer<UiModel> = diff {
        diff(
            get = ::getFavoritesBadge,
            set = ::setFavoritesBadge
        )
    }

    override fun handle(label: BottomNavigationBarStore.Label) {
        when (label) {
            BottomNavigationBarStore.Label.NavigateToMain -> navigateToMain()
            BottomNavigationBarStore.Label.NavigateToFavorites -> navigateToFavorites()
        }
    }

    private fun initFavoritesBadge() {
        favoritesBadge.backgroundColor = context.getColor(res_R.color.pink_transperent)
        favoritesBadge.badgeTextColor = context.getColor(res_R.color.black)
    }

    private fun initOnItemSelectedListener() {
        viewBinding.bottomNavMenu.setOnItemSelectedListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.anime_list -> {
                    dispatch(BottomNavigationBarStore.Intent.MainSectionClick)
                    true
                }

                R.id.anime_favorites -> {
                    dispatch(BottomNavigationBarStore.Intent.FavoritesSectionClick)
                    true
                }

                else -> false
            }
        }
    }

    private fun initOnDestinationChangeListener() {
        val listener = object : NavController.OnDestinationChangedListener {
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
            ) {
                with(viewBinding.bottomNavMenu) {
                    when (destination.id) {
                        R.id.anime_list -> {
                            dispatch(
                                BottomNavigationBarStore.Intent.ChangeSelectedSection(
                                    selectedSection = SectionDomain.MAIN
                                )
                            )
                            if (selectedItemId != R.id.anime_list) {
                                selectedItemId = R.id.anime_list
                            }
                        }

                        R.id.anime_favorites -> {
                            dispatch(
                                BottomNavigationBarStore.Intent.ChangeSelectedSection(
                                    selectedSection = SectionDomain.FAVORITES
                                )
                            )
                            if (selectedItemId != R.id.anime_favorites) {
                                selectedItemId = R.id.anime_favorites
                            }
                        }
                    }
                }
            }
        }
        navController.addOnDestinationChangedListener(listener)
    }

    private fun getFavoritesBadge(uiModel: UiModel): Int {
        return uiModel.favoritesBadgeNumber
    }

    private fun setFavoritesBadge(badgeNumber: Int) {
        if (badgeNumber > 0) {
            favoritesBadge.number = badgeNumber
            favoritesBadge.isVisible = true
        } else {
            favoritesBadge.isVisible = false
        }
    }

    private fun navigateToMain() {
        if (navController.currentDestination?.id != R.id.anime_list) {
            navController.navigate(R.id.anime_list)
        }
    }

    private fun navigateToFavorites() {
        if (navController.currentDestination?.id != R.id.anime_favorites) {
            navController.navigate(R.id.anime_favorites)
        }
    }
}
