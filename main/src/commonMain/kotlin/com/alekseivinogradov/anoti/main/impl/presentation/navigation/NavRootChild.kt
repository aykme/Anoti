package com.alekseivinogradov.anoti.main.impl.presentation.navigation

import com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.navigation.NavAnimeFavoritesScreenComponent
import com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.navigation.NavAnimeListScreenComponent
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.model.SectionDomain

/**
 * The concrete screen component for each `NavRootConfig`, plus its bottom-nav [SectionDomain].
 * It is the single source of truth for that correspondence, so no caller maps a screen to a
 * section by hand. Lives here (not in `core-kmp:navigation`) because it's the only module with a
 * legitimate dependency on both feature modules.
 */
sealed interface NavRootChild {
    val section: SectionDomain

    data class List(val component: NavAnimeListScreenComponent) : NavRootChild {
        override val section: SectionDomain = SectionDomain.MAIN
    }

    data class Favorites(val component: NavAnimeFavoritesScreenComponent) : NavRootChild {
        override val section: SectionDomain = SectionDomain.FAVORITES
    }
}
