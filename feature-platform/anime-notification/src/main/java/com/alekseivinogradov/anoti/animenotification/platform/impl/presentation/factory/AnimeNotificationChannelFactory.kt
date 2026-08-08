package com.alekseivinogradov.anoti.animenotification.platform.impl.presentation.factory

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.alekseivinogradov.anoti.animenotification.platform.R
import javax.inject.Inject

class AnimeNotificationChannelFactory @Inject constructor() {

    fun create(appContext: Context): NotificationChannel {
        return NotificationChannel(
            /* id = */ channelId,
            /* name = */ appContext.getString(R.string.anime_notification_channel),
            /* importance = */ NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = appContext.getString(R.string.anime_notification_channel_description)
            enableVibration(true)
        }
    }
}

const val channelId = "ANIME_NOTIFICATION_CHANNEL_ID"
