package com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.di

/**
 * Exposes [AnimeFavoritesComponent.Factory] to callers that only hold an `Activity` reference.
 * Implemented by the Activity hosting `AnimeFavoritesFragment`; the fragment reads
 * [animeFavoritesComponentFactory] through this interface instead of depending on the concrete
 * Activity class (which lives in a module that depends on this one).
 */
interface AnimeFavoritesComponentFactoryHolder {
    val animeFavoritesComponentFactory: AnimeFavoritesComponent.Factory
}
