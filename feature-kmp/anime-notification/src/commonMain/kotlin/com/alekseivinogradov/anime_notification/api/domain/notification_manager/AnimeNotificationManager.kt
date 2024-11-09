package com.alekseivinogradov.anime_notification.api.domain.notification_manager

interface AnimeNotificationManager {

    companion object {
        const val channelName = "ANIME_NOTIFICATION_CHANNEL"
        const val channelId = "ANIME_NOTIFICATION_CHANNEL_ID"
        const val channelDescription = "Notifications about anime"
    }

    fun makeNewEpisodeNotification(
        animeName: String?,
        episodesAired: Int?,
        imageUrl: String?
    )
}
