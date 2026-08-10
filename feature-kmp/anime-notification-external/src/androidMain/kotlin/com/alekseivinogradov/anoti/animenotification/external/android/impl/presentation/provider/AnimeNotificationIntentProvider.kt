package com.alekseivinogradov.anoti.animenotification.external.android.impl.presentation.provider

import android.app.PendingIntent
import android.content.Context
import com.alekseivinogradov.anoti.navigation.platform.api.presentation.AnimeFavoritesDeepLinkNavigator
import javax.inject.Inject

/**
 * A class for representing dependencies on the "main" module for the "anime-notification" module.
 * It is necessary that the target module does not know directly about "main".
 */
class AnimeNotificationIntentProvider @Inject constructor(
    private val animeFavoritesDeepLinkNavigator: AnimeFavoritesDeepLinkNavigator
) {
    fun getNewEpisodeNotificationIntent(appContext: Context): PendingIntent {
        return animeFavoritesDeepLinkNavigator.getAnimeFavoritesDeepLinkIntent(appContext)
    }
}
