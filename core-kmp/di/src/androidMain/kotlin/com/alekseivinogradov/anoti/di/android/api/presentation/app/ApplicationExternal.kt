package com.alekseivinogradov.anoti.di.android.api.presentation.app

/**
 * Exposes the app's [AppComponent] to callers that only hold an `Application` reference.
 * Implemented by the app's `Application` subclass; other modules read [appComponent] through
 * this interface instead of depending on the concrete `Application` class.
 */
interface ApplicationExternal {
    val appComponent: AppComponent
}
