package com.alekseivinogradov.anoti.animenotification.external.android.impl.presentation.provider.fake

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.alekseivinogradov.anoti.animenotification.external.android.impl.presentation.provider.AnimeNotificationIntentProvider

/** Hands out an intent that opens nothing, for a notification a test posts but never taps. */
object AnimeNotificationIntentProviderFake : AnimeNotificationIntentProvider {
    override fun getNewEpisodeNotificationIntent(appContext: Context): PendingIntent =
        PendingIntent.getActivity(
            appContext,
            0,
            Intent(),
            PendingIntent.FLAG_IMMUTABLE
        )
}
