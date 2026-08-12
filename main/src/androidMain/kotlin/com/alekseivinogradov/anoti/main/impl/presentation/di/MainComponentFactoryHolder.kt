package com.alekseivinogradov.anoti.main.impl.presentation.di

/**
 * Exposes [MainComponent.Factory] to callers that only hold an `Application` reference.
 * Implemented by the app's `Application` subclass; `MainActivity` reads
 * [mainComponentFactory] through this interface instead of depending on the concrete
 * `Application` class (which lives in a module that depends on this one).
 */
interface MainComponentFactoryHolder {
    val mainComponentFactory: MainComponent.Factory
}
