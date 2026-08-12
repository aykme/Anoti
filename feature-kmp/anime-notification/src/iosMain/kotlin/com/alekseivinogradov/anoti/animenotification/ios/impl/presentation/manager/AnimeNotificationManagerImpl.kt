package com.alekseivinogradov.anoti.animenotification.ios.impl.presentation.manager

import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.episode_aired
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

/**
 * Posts a local notification via `UNUserNotificationCenter`. Unlike Android, iOS has no
 * cross-app deep-link intent concept equivalent to `AnimeNotificationIntentProvider` — tapping
 * the notification is wired up separately once an iOS UI/navigation layer exists.
 *
 * Note: unlike the Android implementation, this does not attach a poster image (Glide is an
 * Android-only library); loading a remote image into a `UNNotificationAttachment` requires
 * downloading it to a local file first, which needs a decision on an iOS image-loading library —
 * a known, deliberate gap, out of scope for a DI migration.
 */
class AnimeNotificationManagerImpl(
    private val coroutineContextProvider: CoroutineContextProvider
) : AnimeNotificationManager {
    override fun makeNewEpisodeNotification(
        animeName: String?,
        airedEpisode: Int?,
        imageUrl: String?
    ) {
        val episodeAiredString = runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(Res.string.episode_aired)
        }
        val content = UNMutableNotificationContent().apply {
            setTitle(animeName ?: "")
            setBody("$episodeAiredString: ${airedEpisode ?: ""}")
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "ANIME_NOTIFICATION_${animeName}_$airedEpisode",
            content = content,
            trigger = null
        )
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { }
    }
}
