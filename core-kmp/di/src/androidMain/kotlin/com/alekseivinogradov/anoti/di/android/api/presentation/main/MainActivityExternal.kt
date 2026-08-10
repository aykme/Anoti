package com.alekseivinogradov.anoti.di.android.api.presentation.main

/**
 * Exposes the screen's [MainComponent] to callers that only hold an `Activity` reference.
 * Implemented by `:main`'s `MainActivity`; feature Fragments read [mainComponent] through this
 * interface instead of depending on the concrete `MainActivity` class.
 */
interface MainActivityExternal {
    val mainComponent: MainComponent
}
