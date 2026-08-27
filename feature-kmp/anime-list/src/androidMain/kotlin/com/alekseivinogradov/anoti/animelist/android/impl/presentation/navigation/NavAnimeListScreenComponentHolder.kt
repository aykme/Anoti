package com.alekseivinogradov.anoti.animelist.android.impl.presentation.navigation

import com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.navigation.NavAnimeListScreenComponent

/**
 * Exposes the currently active [NavAnimeListScreenComponent] to callers that only hold an
 * `Activity` reference. Implemented by the Activity hosting
 * [com.alekseivinogradov.anoti.animelist.android.impl.presentation.AnimeListFragment];
 * the fragment reads [navAnimeListScreenComponent]
 * through this interface instead of depending on the concrete
 * Activity class (which lives in a module that depends on this one).
 */
interface NavAnimeListScreenComponentHolder {
    val navAnimeListScreenComponent: NavAnimeListScreenComponent
}
