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
import com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.manager.newEpisodeNotificationText
import com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.poster.PosterLoader
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.SilverTransparent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    // The periodic and the one-off update passes can run at the same time, and each id may only
    // be handed out once.
    private val postingMutex = Mutex()

    private val newEpisodesGroupKey = "ANIME_NOTIFICATION_NEW_EPISODE_GROUP_KEY"

    /** Single id should be from [DEFAULT_SINGLE_ID] to [MAX_SINGLE_ID] */
    private var singleId = DEFAULT_SINGLE_ID

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

            postingMutex.withLock {
                notificationManager.notify(
                    /* id = */
                    singleId,
                    /* notification = */
                    singleNotification
                )
                changeSingleIdToNext()

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

    private fun changeSingleIdToNext() {
        if (singleId < MAX_SINGLE_ID) {
            singleId++
        } else {
            singleId = DEFAULT_SINGLE_ID
        }
    }

    private companion object {
        private const val DEFAULT_SINGLE_ID = 10
        private const val MAX_SINGLE_ID = 99
    }
}
