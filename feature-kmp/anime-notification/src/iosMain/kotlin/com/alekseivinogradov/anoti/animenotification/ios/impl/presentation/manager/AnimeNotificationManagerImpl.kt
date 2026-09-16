package com.alekseivinogradov.anoti.animenotification.ios.impl.presentation.manager

import coil3.Image
import coil3.toBitmap
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.episode_aired
import com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.poster.PosterLoader
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import org.jetbrains.skia.EncodedImageFormat
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToURL
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationAttachment
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter
import org.jetbrains.skia.Image as SkiaImage

/**
 * Posts a local notification via `UNUserNotificationCenter`. Unlike Android, iOS has no
 * cross-app deep-link intent concept equivalent to `AnimeNotificationIntentProvider` — tapping
 * the notification is wired up separately once an iOS UI/navigation layer exists.
 */
class AnimeNotificationManagerImpl(
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
     * A `UNNotificationAttachment` can only reference a local file, so the poster is written into
     * the temporary directory first.
     */
    @OptIn(ExperimentalForeignApi::class)
    private suspend fun createPosterAttachment(imageUrl: String?): UNNotificationAttachment? {
        val localUrl = imageUrl
            ?.let { url: String -> savePosterToTemporaryFile(url) }
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
                println("$tag: poster was rejected as an attachment, removing $localUrl")
                NSFileManager.defaultManager.removeItemAtURL(localUrl, null)
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun savePosterToTemporaryFile(imageUrl: String): NSURL? {
        val data = posterLoader.load(imageUrl)?.toPngData() ?: return null
        val posterName = imageUrl.substringAfterLast('/').substringBeforeLast('.')
        val localUrl = NSURL.fileURLWithPath(
            NSTemporaryDirectory() + POSTER_FILE_PREFIX + posterName + POSTER_FILE_EXTENSION
        )
        return if (data.writeToURL(localUrl, atomically = true)) {
            localUrl
        } else {
            println("$tag: poster could not be written to $localUrl")
            null
        }
    }

    // The attachment needs an encoded file, while Coil hands back a decoded bitmap.
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun Image.toPngData(): NSData? {
        val bytes = SkiaImage.makeFromBitmap(toBitmap())
            .encodeToData(EncodedImageFormat.PNG)
            ?.bytes
            ?.takeIf { encoded: ByteArray -> encoded.isNotEmpty() }
            ?: return null
        return bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        }
    }
}

private const val POSTER_FILE_PREFIX = "anime_notification_poster_"
private const val POSTER_FILE_EXTENSION = ".png"
