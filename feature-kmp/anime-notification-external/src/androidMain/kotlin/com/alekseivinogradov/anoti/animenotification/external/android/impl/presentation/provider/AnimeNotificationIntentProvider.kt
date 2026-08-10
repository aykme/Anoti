package com.alekseivinogradov.anoti.animenotification.external.android.impl.presentation.provider

import android.app.PendingIntent
import android.content.Context

/**
 * Builds the notification's deep-link intent. Implemented by the module that owns
 * the navigation graph and the target activity ("main"), so this module and its
 * consumers don't need a compile-time dependency on it.
 */
interface AnimeNotificationIntentProvider {
    fun getNewEpisodeNotificationIntent(appContext: Context): PendingIntent
}
