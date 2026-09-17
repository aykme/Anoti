package com.alekseivinogradov.anoti.animenotification.ios.impl.presentation.manager

import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.episode_aired
import com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.poster.PosterLoader
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationAttachment
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

/**
 * Posts a local notification via `UNUserNotificationCenter`. Unlike Android, iOS has no
 * cross-app deep-link intent concept equivalent to `AnimeNotificationIntentProvider` — tapping
 * the notification is wired up separately once an iOS UI/navigation layer exists.
 */
internal class AnimeNotificationManagerImpl(
    private val coroutineContextProvider: CoroutineContextProvider,
    private val posterLoader: PosterLoader
) : AnimeNotificationManager {

    private val tag = "ANIME_NOTIFICATION_MANAGER"

    override suspend fun makeNewEpisodeNotification(
        animeName: String?,
        airedEpisode: Int?,
        imageUrl: String?
    ) = withContext(coroutineContextProvider.ioDispatcher) {
        val episodeAiredString = getString(Res.string.episode_aired)
        val content = UNMutableNotificationContent().apply {
            setTitle(animeName ?: "")
            setBody("$episodeAiredString: ${airedEpisode ?: ""}")
            createPosterAttachment(imageUrl)?.let { attachment: UNNotificationAttachment ->
                setAttachments(listOf(attachment))
            }
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "ANIME_NOTIFICATION_${animeName}_$airedEpisode",
            content = content,
            trigger = null
        )
        UNUserNotificationCenter.currentNotificationCenter()
            .addNotificationRequest(request) { error: NSError? ->
                error?.let { println("$tag: notification was not scheduled: $it") }
            }
    }

    /**
     * A `UNNotificationAttachment` can only reference a local file, so the downloaded poster file
     * is copied into the temporary directory first.
     */
    @OptIn(ExperimentalForeignApi::class)
    private suspend fun createPosterAttachment(imageUrl: String?): UNNotificationAttachment? {
        val posterFile = posterLoader.loadFile(imageUrl) ?: return null
        val localUrl = NSURL.fileURLWithPath(posterFile.toString())

        return UNNotificationAttachment.attachmentWithIdentifier(
            identifier = posterFile.name,
            URL = localUrl,
            options = null,
            error = null
        ).also { attachment: UNNotificationAttachment? ->
            // A rejected file is never moved into the attachment store, so it would stay in the
            // temporary directory for good.
            if (attachment == null) {
                println("$tag: poster was rejected as an attachment, removing $localUrl")
                NSFileManager.defaultManager.removeItemAtURL(localUrl, null)
            }
        }
    }
}
