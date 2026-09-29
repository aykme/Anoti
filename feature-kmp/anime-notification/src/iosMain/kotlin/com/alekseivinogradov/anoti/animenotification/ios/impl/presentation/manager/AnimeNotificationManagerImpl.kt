package com.alekseivinogradov.anoti.animenotification.ios.impl.presentation.manager

import com.alekseivinogradov.anoti.animenotification.external.kmp.api.presentation.provider.AnimeNotificationTapPayloadProvider
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.manager.NEW_EPISODES_GROUP_KEY
import com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.manager.NotificationIdRing
import com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.manager.ShownNotification
import com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.manager.newEpisodeNotificationText
import com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.manager.ringNotificationIdentifier
import com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.manager.shownNotificationOf
import com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.poster.PosterLoader
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationAttachment
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "ANIME_NOTIFICATION_MANAGER"

/**
 * Posts new-episode notifications through `UNUserNotificationCenter`, the iOS side of what the
 * Android manager does. They take their ids from the same [NotificationIdRing], play the default
 * sound and show as one group. Each carries the payload a tap opens the app with.
 *
 * @param animeNotificationTapPayloadProvider builds what a tap hands back to the app.
 */
internal class AnimeNotificationManagerImpl(
    private val coroutineContextProvider: CoroutineContextProvider,
    private val posterLoader: PosterLoader,
    private val animeNotificationTapPayloadProvider: AnimeNotificationTapPayloadProvider
) : AnimeNotificationManager {

    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

    // The periodic and the one-off update passes can run at the same time, and the ring hands
    // each of them its own id.
    private val singleIds = NotificationIdRing(shownNotifications = ::readShownNotifications)

    override suspend fun makeNewEpisodeNotification(
        animeName: String?,
        airedEpisode: Int?,
        imageUrl: String?
    ) = withContext(coroutineContextProvider.ioDispatcher) {
        val text = newEpisodeNotificationText(
            animeName = animeName,
            airedEpisode = airedEpisode
        )
        val tapPayload = animeNotificationTapPayloadProvider.getNewEpisodeTapPayload()
        val content = UNMutableNotificationContent().apply {
            setTitle(text.title)
            setBody(text.body)
            setSound(UNNotificationSound.defaultSound)
            setThreadIdentifier(NEW_EPISODES_GROUP_KEY)
            setUserInfo(tapPayload.toMap<Any?, Any?>())
            createPosterAttachment(imageUrl)?.let { attachment: UNNotificationAttachment ->
                setAttachments(listOf(attachment))
            }
        }
        singleIds.withNextId { id: Int ->
            // An identifier already in use replaces the notification shown under it.
            post(
                UNNotificationRequest.requestWithIdentifier(
                    identifier = ringNotificationIdentifier(id),
                    content = content,
                    trigger = null
                )
            )
        }
    }

    // Waits for the system to take the request, and a cancellation cannot cut the wait short,
    // so the ring records every id the system was handed. A refused request still counts.
    private suspend fun post(request: UNNotificationRequest) = suspendCoroutine { continuation ->
        notificationCenter.addNotificationRequest(request) { error: NSError? ->
            error?.let { println("$TAG: notification was not scheduled: $it") }
            continuation.resume(Unit)
        }
    }

    // The list lacks a request added a moment ago. The ring covers that for the id it handed out
    // last.
    private suspend fun readShownNotifications(): List<ShownNotification> =
        suspendCancellableCoroutine { continuation ->
            notificationCenter.getDeliveredNotificationsWithCompletionHandler { delivered ->
                continuation.resume(
                    delivered.orEmpty()
                        .filterIsInstance<UNNotification>()
                        .mapNotNull { notification: UNNotification ->
                            shownNotificationOf(
                                identifier = notification.request.identifier,
                                deliveredAtEpochSeconds = notification.date.timeIntervalSince1970
                            )
                        }
                )
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
                println("$TAG: poster was rejected as an attachment, removing $localUrl")
                NSFileManager.defaultManager.removeItemAtURL(localUrl, null)
            }
        }
    }
}
