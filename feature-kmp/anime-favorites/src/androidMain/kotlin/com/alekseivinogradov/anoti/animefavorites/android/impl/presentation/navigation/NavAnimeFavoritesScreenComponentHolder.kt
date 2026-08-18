package com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.navigation

/**
 * Exposes the currently active [NavAnimeFavoritesScreenComponent] to callers that only hold an
 * `Activity` reference. Implemented by the Activity hosting
 * [com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.AnimeFavoritesFragment];
 * the fragment reads [navAnimeFavoritesScreenComponent] through this interface instead of depending on
 * the concrete Activity class (which lives in a module that depends on this one).
 */
interface NavAnimeFavoritesScreenComponentHolder {
    val navAnimeFavoritesScreenComponent: NavAnimeFavoritesScreenComponent
}
