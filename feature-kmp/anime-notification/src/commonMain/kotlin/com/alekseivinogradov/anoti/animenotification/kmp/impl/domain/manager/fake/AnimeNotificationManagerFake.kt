package com.alekseivinogradov.anoti.animenotification.kmp.impl.domain.manager.fake

import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager

/** One posted new-episode notification, as the code under test asked for it. */
data class NewEpisodeNotification(
    val animeName: String?,
    val airedEpisode: Int?,
    val imageUrl: String?
)

/**
 * Shows nothing and records the request instead, so a test asserts what the code under test
 * wanted the user to see without a notification manager or a poster download.
 *
 * @param onNotify runs before the call is recorded. Suspend in it to hold a poster download
 * open, or throw from it to stand in for one that fails.
 */
class AnimeNotificationManagerFake(
    private val onNotify: suspend () -> Unit = {}
) : AnimeNotificationManager {

    /** Every notification asked for, oldest first. */
    val notifications: List<NewEpisodeNotification>
        field = mutableListOf<NewEpisodeNotification>()

    override suspend fun makeNewEpisodeNotification(
        animeName: String?,
        airedEpisode: Int?,
        imageUrl: String?
    ) {
        onNotify()
        notifications += NewEpisodeNotification(
            animeName = animeName,
            airedEpisode = airedEpisode,
            imageUrl = imageUrl
        )
    }
}
