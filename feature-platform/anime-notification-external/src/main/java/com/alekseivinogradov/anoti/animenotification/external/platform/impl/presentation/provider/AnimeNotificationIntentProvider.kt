package com.alekseivinogradov.anoti.animenotification.external.platform.impl.presentation.provider

import android.app.PendingIntent
import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import com.alekseivinogradov.anoti.main.R as main_R
import com.alekseivinogradov.anoti.main.impl.presentation.MainActivity
import javax.inject.Inject

/**
 * A class for representing dependencies on the "main" module for the "anime-notification" module.
 * It is necessary that the target module does not know directly about "main".
 */
class AnimeNotificationIntentProvider @Inject constructor() {
    fun getNewEpisodeNotificationIntent(appContext: Context): PendingIntent {
        return NavDeepLinkBuilder(appContext)
            .setComponentName(MainActivity::class.java)
            .setGraph(main_R.navigation.nav_graph)
            .setDestination(main_R.id.anime_favorites)
            .createPendingIntent()
    }
}
