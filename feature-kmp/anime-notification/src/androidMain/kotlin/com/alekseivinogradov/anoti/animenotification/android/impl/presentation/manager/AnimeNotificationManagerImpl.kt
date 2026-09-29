package com.alekseivinogradov.anoti.animenotification.android.impl.presentation.manager

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import coil3.toBitmap
import com.alekseivinogradov.anoti.animenotification.android.impl.presentation.factory.CHANNEL_ID
import com.alekseivinogradov.anoti.animenotification.external.android.impl.presentation.provider.AnimeNotificationIntentProvider
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.new_episodes
import com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.manager.NotificationIdRing
import com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.manager.ShownNotification
import com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.manager.newEpisodeNotificationText
import com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.poster.PosterLoader
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.SilverTransparent
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import com.alekseivinogradov.anoti.celebrity.kmp.R as res_R

internal class AnimeNotificationManagerImpl(
    private val appContext: Context,
    animeNotificationIntentProvider: AnimeNotificationIntentProvider,
    private val coroutineContextProvider: CoroutineContextProvider,
    private val posterLoader: PosterLoader
) : AnimeNotificationManager {

    private val iconColor: Int = SilverTransparent.toArgb()

    private val intent: PendingIntent =
        animeNotificationIntentProvider.getNewEpisodeNotificationIntent(appContext)

    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(appContext)

    private val newEpisodesGroupKey = "ANIME_NOTIFICATION_NEW_EPISODE_GROUP_KEY"

    // The periodic and the one-off update passes can run at the same time, and the ring hands
    // each of them its own id.
    private val singleIds = NotificationIdRing(shownNotifications = ::readShownNotifications)

    /** Group ids should be from 0 to 9 */
    private val newEpisodesSummaryId = 0

    @SuppressLint("MissingPermission")
    override suspend fun makeNewEpisodeNotification(
        animeName: String?,
        airedEpisode: Int?,
        imageUrl: String?
    ) {
        withContext(coroutineContextProvider.ioDispatcher) {
            val text = newEpisodeNotificationText(
                animeName = animeName,
                airedEpisode = airedEpisode
            )
            val singleNotification = buildSingleNotification(
                title = text.title,
                contentText = text.body,
                poster = posterLoader.loadImage(imageUrl)?.toBitmap()
            )
            val summaryNotification = buildSummaryNotification()

            singleIds.withNextId { singleId: Int ->
                notificationManager.notify(
                    /* id = */
                    singleId,
                    /* notification = */
                    singleNotification
                )

                notificationManager.notify(
                    /* id = */
                    newEpisodesSummaryId,
                    /* notification = */
                    summaryNotification
                )
            }
        }
    }

    private fun buildSingleNotification(
        title: String,
        contentText: String,
        poster: Bitmap?
    ): Notification = newEpisodeBuilder()
        .setAutoCancel(true)
        .setContentIntent(intent)
        .setContentTitle(title)
        .setContentText(contentText)
        .setLargeIcon(poster)
        .build()

    private suspend fun buildSummaryNotification(): Notification = newEpisodeBuilder()
        .setGroupSummary(true)
        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
        .setStyle(
            NotificationCompat.InboxStyle()
                .setSummaryText(getString(Res.string.new_episodes))
        )
        .build()

    private fun newEpisodeBuilder(): NotificationCompat.Builder = NotificationCompat.Builder(
        /* context = */
        appContext,
        /* channelId = */
        CHANNEL_ID
    )
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setGroup(newEpisodesGroupKey)
        .setColor(iconColor)
        .setColorized(true)
        .setSmallIcon(res_R.mipmap.ic_notification)

    // A notification is identified by its tag and id together. Only untagged ones are this
    // manager's, and the tagged group summary the system may add is not.
    private fun readShownNotifications(): List<ShownNotification> =
        notificationManager.activeNotifications
            .filter { it.tag == null }
            .map { ShownNotification(id = it.id, postedAtMillis = it.postTime) }
}
