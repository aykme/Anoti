package com.alekseivinogradov.anoti.main.impl.presentation.navigation

import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.navigation.NavAnimeFavoritesScreenComponent
import com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.navigation.NavAnimeListScreenComponent

/**
 * The concrete screen component for each `NavRootConfig`. Lives here (not in
 * `core-kmp:navigation`) because it's the only module with a legitimate dependency on both
 * feature modules.
 */
sealed interface NavRootChild {
    data class List(val component: NavAnimeListScreenComponent) : NavRootChild
    data class Favorites(val component: NavAnimeFavoritesScreenComponent) : NavRootChild
}
