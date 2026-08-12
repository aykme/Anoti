package com.alekseivinogradov.anoti.main.impl.presentation.navigation

import com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.di.AnimeFavoritesScreenComponent
import com.alekseivinogradov.anoti.animelist.android.impl.presentation.di.AnimeListScreenComponent

/**
 * The concrete screen component for each `RootConfig`. Lives here (not in `core-kmp:navigation`)
 * because it's the only module with a legitimate dependency on both feature modules.
 */
sealed interface RootChild {
    data class List(val component: AnimeListScreenComponent) : RootChild
    data class Favorites(val component: AnimeFavoritesScreenComponent) : RootChild
}
