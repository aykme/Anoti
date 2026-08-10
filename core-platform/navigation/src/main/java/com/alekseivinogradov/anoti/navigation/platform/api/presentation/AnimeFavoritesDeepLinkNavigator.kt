package com.alekseivinogradov.anoti.navigation.platform.api.presentation

import android.app.PendingIntent
import android.content.Context

/**
 * Builds the [PendingIntent] that deep-links into the anime favorites screen.
 * Implemented by the module that owns the navigation graph and the target
 * activity, so callers don't need a compile-time dependency on it.
 */
interface AnimeFavoritesDeepLinkNavigator {
    fun getAnimeFavoritesDeepLinkIntent(appContext: Context): PendingIntent
}
