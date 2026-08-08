package com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager

/**
 * Shows the "new episode aired" notification.
 */
interface AnimeNotificationManager {

    /**
     * @param animeName anime title to show, or null if unknown.
     * @param airedEpisode number of the aired episode, or null if unknown.
     * @param imageUrl anime cover image to show, or null if unavailable.
     */
    fun makeNewEpisodeNotification(
        animeName: String?,
        airedEpisode: Int?,
        imageUrl: String?
    )
}
