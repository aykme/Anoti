package com.alekseivinogradov.anoti.animelist.android.impl.presentation.di

/**
 * Exposes the currently active [AnimeListScreenComponent] to callers that only hold an
 * `Activity` reference. Implemented by the Activity hosting
 * [com.alekseivinogradov.anoti.animelist.android.impl.presentation.AnimeListFragment];
 * the fragment reads [animeListScreenComponent]
 * through this interface instead of depending on the concrete
 * Activity class (which lives in a module that depends on this one).
 */
interface AnimeListScreenComponentHolder {
    val animeListScreenComponent: AnimeListScreenComponent
}
