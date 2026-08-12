package com.alekseivinogradov.anoti.main.impl.presentation

import android.content.Context
import android.view.MenuItem
import android.view.View
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.model.SectionDomain
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.UiModel
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation.BottomNavigationBarView
import com.alekseivinogradov.anoti.main.R
import com.alekseivinogradov.anoti.main.impl.presentation.navigation.RootChild
import com.alekseivinogradov.anoti.navigation.kmp.RootComponent
import com.alekseivinogradov.anoti.navigation.kmp.RootConfig
import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.subscribe
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.alekseivinogradov.anoti.celebrity.kmp.R as res_R

internal class BottomNavigationBarViewImpl(
    private val rootView: View,
    private val rootComponent: RootComponent<RootChild>,
    private val lifecycle: Lifecycle
) : BottomNavigationBarView, BaseMviView<UiModel, BottomNavigationBarStore.Intent>() {

    private val context: Context
        get() = rootView.context

    private val bottomNavMenu: BottomNavigationView = rootView.findViewById(R.id.bottom_nav_menu)

    private val favoritesBadge: BadgeDrawable =
        bottomNavMenu.getOrCreateBadge(R.id.anime_favorites)

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
        favoritesBadge.backgroundColor = context.getColor(res_R.color.cinnabar_500_transparent)
        favoritesBadge.badgeTextColor = context.getColor(res_R.color.black)
    }

    private fun initOnItemSelectedListener() {
        bottomNavMenu.setOnItemSelectedListener { menuItem: MenuItem ->
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
        rootComponent.childStack.subscribe(lifecycle, ObserveLifecycleMode.CREATE_DESTROY) { stack ->
            with(bottomNavMenu) {
                when (stack.active.instance) {
                    is RootChild.List -> {
                        dispatch(
                            BottomNavigationBarStore.Intent.ChangeSelectedSection(
                                selectedSection = SectionDomain.MAIN
                            )
                        )
                        if (selectedItemId != R.id.anime_list) {
                            selectedItemId = R.id.anime_list
                        }
                    }

                    is RootChild.Favorites -> {
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
        if (rootComponent.childStack.value.active.instance !is RootChild.List) {
            rootComponent.navigateTo(RootConfig.AnimeList)
        }
    }

    private fun navigateToFavorites() {
        if (rootComponent.childStack.value.active.instance !is RootChild.Favorites) {
            rootComponent.navigateTo(RootConfig.AnimeFavorites)
        }
    }
}
