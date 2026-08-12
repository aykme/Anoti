package com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.di

/**
 * Exposes the currently active [AnimeFavoritesScreenComponent] to callers that only hold an
 * `Activity` reference. Implemented by the Activity hosting [AnimeFavoritesFragment]; the
 * fragment reads [animeFavoritesScreenComponent] through this interface instead of depending on
 * the concrete Activity class (which lives in a module that depends on this one).
 */
interface AnimeFavoritesScreenComponentHolder {
    val animeFavoritesScreenComponent: AnimeFavoritesScreenComponent
}
