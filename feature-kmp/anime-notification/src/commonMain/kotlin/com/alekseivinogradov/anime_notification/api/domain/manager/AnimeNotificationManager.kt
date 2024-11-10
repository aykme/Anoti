package com.alekseivinogradov.anime_notification.api.domain.manager

interface AnimeNotificationManager {

    fun makeNewEpisodeNotification(
        animeName: String?,
        airedEpisode: Int?,
        imageUrl: String?
    )
}
