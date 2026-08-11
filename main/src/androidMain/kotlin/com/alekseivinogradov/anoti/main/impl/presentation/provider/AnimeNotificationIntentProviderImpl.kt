package com.alekseivinogradov.anoti.main.impl.presentation.provider

import android.app.PendingIntent
import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import com.alekseivinogradov.anoti.animenotification.external.android.impl.presentation.provider.AnimeNotificationIntentProvider
import com.alekseivinogradov.anoti.main.R as main_R
import com.alekseivinogradov.anoti.main.impl.presentation.MainActivity
import com.alekseivinogradov.anoti.navigation.R as navigation_R
import javax.inject.Inject

/**
 * The "main"-side implementation of [AnimeNotificationIntentProvider], bound into
 * `:app`'s Dagger graph by `AnimeNotificationIntentProviderModule`.
 */
class AnimeNotificationIntentProviderImpl @Inject constructor() : AnimeNotificationIntentProvider {
    override fun getNewEpisodeNotificationIntent(appContext: Context): PendingIntent {
        return NavDeepLinkBuilder(appContext)
            .setComponentName(MainActivity::class.java)
            .setGraph(navigation_R.navigation.nav_graph)
            .setDestination(main_R.id.anime_favorites)
            .createPendingIntent()
    }
}
