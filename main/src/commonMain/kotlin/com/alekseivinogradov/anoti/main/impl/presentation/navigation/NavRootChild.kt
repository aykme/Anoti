package com.alekseivinogradov.anoti.main.impl.presentation.navigation

import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.navigation.NavAnimeFavoritesScreenComponent
import com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.navigation.NavAnimeListScreenComponent
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.model.SectionDomain
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig

/**
 * The concrete screen component for each `NavRootConfig`, plus that config and its bottom-nav
 * [SectionDomain] — the single source of truth for both correspondences, instead of every
 * caller mapping [NavRootConfig]/[NavRootChild] to a section by hand. Lives here (not in
 * `core-kmp:navigation`) because it's the only module with a legitimate dependency on both
 * feature modules.
 */
sealed interface NavRootChild {
    val config: NavRootConfig
    val section: SectionDomain

    data class List(val component: NavAnimeListScreenComponent) : NavRootChild {
        override val config: NavRootConfig = NavRootConfig.AnimeList
        override val section: SectionDomain = SectionDomain.MAIN
    }

    data class Favorites(val component: NavAnimeFavoritesScreenComponent) : NavRootChild {
        override val config: NavRootConfig = NavRootConfig.AnimeFavorites
        override val section: SectionDomain = SectionDomain.FAVORITES
    }
}
