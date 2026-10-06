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
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.ANOTI_TAG
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okio.Path
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNErrorCodeAttachmentCorrupt
import platform.UserNotifications.UNErrorCodeAttachmentInvalidURL
import platform.UserNotifications.UNErrorDomain
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationAttachment
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "AnimeNotification"

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
        val posterFile = posterLoader.loadFile(imageUrl)
        val poster = posterFile?.let(::createPosterAttachment)
        val content = UNMutableNotificationContent().apply {
            setTitle(text.title)
            setBody(text.body)
            setSound(UNNotificationSound.defaultSound)
            setThreadIdentifier(NEW_EPISODES_GROUP_KEY)
            setUserInfo(tapPayload.toMap<Any?, Any?>())
            poster?.let { attachment: UNNotificationAttachment ->
                setAttachments(listOf(attachment))
            }
        }
        // The system moves a poster it takes out of the temporary directory. One it never took
        // would stay there.
        var isPosterTaken = false
        try {
            singleIds.withNextId { id: Int ->
                // An identifier already in use replaces the notification shown under it.
                val identifier = ringNotificationIdentifier(id)
                val error = post(identifier = identifier, content = content)
                isPosterTaken = poster != null && error == null
                if (poster != null && error?.isRefusedAttachment() == true) {
                    println("$ANOTI_TAG $TAG: the poster was refused, posting without it: $error")
                    content.setAttachments(emptyList<UNNotificationAttachment>())
                    post(identifier = identifier, content = content)
                }
            }
        } finally {
            if (!isPosterTaken) {
                posterFile?.let(::deleteLeftover)
            }
        }
    }

    // Waits for the system to take the request, and a cancellation cannot cut the wait short,
    // so the ring records every id the system was handed. A refused request still counts.
    private suspend fun post(identifier: String, content: UNMutableNotificationContent): NSError? =
        suspendCoroutine { continuation ->
            val request = UNNotificationRequest.requestWithIdentifier(
                identifier = identifier,
                content = content,
                trigger = null
            )
            notificationCenter.addNotificationRequest(request) { error: NSError? ->
                if (error == null) {
                    println("$ANOTI_TAG $TAG: notification $identifier handed to the system")
                } else {
                    println("$ANOTI_TAG $TAG: notification was not scheduled: $error")
                }
                continuation.resume(error)
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
     * A `UNNotificationAttachment` can only reference a local file, which is why the poster is a
     * copy in the temporary directory.
     */
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun createPosterAttachment(posterFile: Path): UNNotificationAttachment? = memScoped {
        val error = alloc<ObjCObjectVar<NSError?>>()
        UNNotificationAttachment.attachmentWithIdentifier(
            identifier = posterFile.name,
            URL = NSURL.fileURLWithPath(posterFile.toString()),
            options = null,
            error = error.ptr
        ).also { attachment: UNNotificationAttachment? ->
            if (attachment == null) {
                println("$ANOTI_TAG $TAG: the poster was rejected as an attachment: ${error.value}")
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun deleteLeftover(posterFile: Path) {
        val fileManager = NSFileManager.defaultManager
        val path = posterFile.toString()
        if (fileManager.fileExistsAtPath(path)) {
            fileManager.removeItemAtPath(path, null)
        }
    }
}

// The codes of a request refused for one of its attachments.
private fun NSError.isRefusedAttachment(): Boolean =
    domain == UNErrorDomain && code in UNErrorCodeAttachmentInvalidURL..UNErrorCodeAttachmentCorrupt
