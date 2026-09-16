package com.alekseivinogradov.anoti.animenotification.ios.impl.presentation.manager

import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.episode_aired
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSLog
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.writeToURL
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationAttachment
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

/**
 * Posts a local notification via `UNUserNotificationCenter`. Unlike Android, iOS has no
 * cross-app deep-link intent concept equivalent to `AnimeNotificationIntentProvider` — tapping
 * the notification is wired up separately once an iOS UI/navigation layer exists.
 */
class AnimeNotificationManagerImpl(
    private val coroutineContextProvider: CoroutineContextProvider
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
                error?.let { NSLog("$tag: notification was not scheduled: $it") }
            }
    }

    /**
     * A `UNNotificationAttachment` can only reference a local file, so the poster is copied into
     * the temporary directory first.
     */
    @OptIn(ExperimentalForeignApi::class)
    private suspend fun createPosterAttachment(imageUrl: String?): UNNotificationAttachment? {
        val localUrl = imageUrl
            ?.let { url: String -> NSURL.URLWithString(url) }
            ?.let { remoteUrl: NSURL -> savePosterToTemporaryFile(remoteUrl) }
            ?: return null

        return UNNotificationAttachment.attachmentWithIdentifier(
            identifier = localUrl.lastPathComponent ?: POSTER_FILE_PREFIX,
            URL = localUrl,
            options = null,
            error = null
        ).also { attachment: UNNotificationAttachment? ->
            // A rejected file is never moved into the attachment store, so it would stay in the
            // temporary directory for good.
            if (attachment == null) {
                NSLog("$tag: poster was rejected as an attachment, removing $localUrl")
                NSFileManager.defaultManager.removeItemAtURL(localUrl, null)
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun savePosterToTemporaryFile(remoteUrl: NSURL): NSURL? {
        val data = downloadPoster(remoteUrl) ?: return null
        val fileName = POSTER_FILE_PREFIX + (remoteUrl.lastPathComponent ?: "image")
        val localUrl = NSURL.fileURLWithPath(NSTemporaryDirectory() + fileName)
        return if (data.writeToURL(localUrl, atomically = true)) {
            localUrl
        } else {
            NSLog("$tag: poster could not be written to $localUrl")
            null
        }
    }

    /**
     * `NSURLSession` rather than `NSData.dataWithContentsOfURL`: the latter blocks the calling
     * thread with no timeout and ignores cancellation, which a background refresh cannot afford.
     */
    private suspend fun downloadPoster(remoteUrl: NSURL): NSData? {
        val result = CompletableDeferred<NSData?>()
        val request = NSURLRequest.requestWithURL(
            URL = remoteUrl,
            cachePolicy = 0u,
            timeoutInterval = POSTER_TIMEOUT_SECONDS
        )
        NSURLSession.sharedSession.dataTaskWithRequest(request) {
                data: NSData?, _: NSURLResponse?, error: NSError? ->
            error?.let { NSLog("$tag: poster download failed: $it") }
            result.complete(data)
        }.resume()
        return result.await()
    }
}

private const val POSTER_FILE_PREFIX = "anime_notification_poster_"
private const val POSTER_TIMEOUT_SECONDS = 10.0
