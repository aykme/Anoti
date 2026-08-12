package com.alekseivinogradov.anoti.animelist.android.impl.presentation.di

/**
 * Exposes [AnimeListComponent.Factory] to callers that only hold an `Activity` reference.
 * Implemented by the Activity hosting `AnimeListFragment`; the fragment reads
 * [animeListComponentFactory] through this interface instead of depending on the concrete
 * Activity class (which lives in a module that depends on this one).
 */
interface AnimeListComponentFactoryHolder {
    val animeListComponentFactory: AnimeListComponent.Factory
}
