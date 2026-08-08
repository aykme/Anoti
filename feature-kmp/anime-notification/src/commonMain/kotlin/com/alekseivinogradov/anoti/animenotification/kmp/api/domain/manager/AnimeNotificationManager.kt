package com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager

interface AnimeNotificationManager {

    fun makeNewEpisodeNotification(
        animeName: String?,
        airedEpisode: Int?,
        imageUrl: String?
    )
}
