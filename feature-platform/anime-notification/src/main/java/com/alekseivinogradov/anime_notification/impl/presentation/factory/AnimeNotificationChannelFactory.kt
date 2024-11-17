package com.alekseivinogradov.anime_notification.impl.presentation.factory

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.alekseivinogradov.anime_notification_platform.R

class AnimeNotificationChannelFactory {

    companion object {
        const val channelId = "ANIME_NOTIFICATION_CHANNEL_ID"
    }

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
