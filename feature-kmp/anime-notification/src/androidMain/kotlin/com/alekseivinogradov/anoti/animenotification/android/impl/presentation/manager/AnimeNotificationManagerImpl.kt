package com.alekseivinogradov.anoti.animenotification.android.impl.presentation.manager

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.alekseivinogradov.anoti.animenotification.android.impl.presentation.factory.CHANNEL_ID
import com.alekseivinogradov.anoti.animenotification.external.android.impl.presentation.provider.AnimeNotificationIntentProvider
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.episode_aired
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.new_episodes
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.SilverTransparent
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.no_data
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import kotlin.coroutines.cancellation.CancellationException
import com.alekseivinogradov.anoti.celebrity.kmp.R as res_R
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res as celebrityRes

class AnimeNotificationManagerImpl(
    private val appContext: Context,
    animeNotificationIntentProvider: AnimeNotificationIntentProvider,
    private val coroutineContextProvider: CoroutineContextProvider
) : AnimeNotificationManager {
    private val tag = "ANIME_NOTIFICATION_MANAGER"

    private val iconColor: Int = SilverTransparent.toArgb()

    // The app-wide loader, so a poster already shown on screen comes from its cache.
    private val imageLoader: ImageLoader by lazy { SingletonImageLoader.get(appContext) }

    private var singleBuilder: NotificationCompat.Builder
    private var intent: PendingIntent? = null
    private var notificationManager: NotificationManagerCompat? = null

    private val newEpisodesGroupKey = "ANIME_NOTIFICATION_NEW_EPISODE_GROUP_KEY"

    /** Single id should be from [DEFAULT_SINGLE_ID] to [MAX_SINGLE_ID] */
    private var singleId = DEFAULT_SINGLE_ID

    /** Group ids should be from 0 to 9 */
    private val newEpisodesSummaryId = 0

    init {
        intent = animeNotificationIntentProvider.getNewEpisodeNotificationIntent(appContext)
        notificationManager = NotificationManagerCompat.from(appContext)

        singleBuilder = NotificationCompat.Builder(
            /* context = */
            appContext,
            /* channelId = */
            CHANNEL_ID
        )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup(newEpisodesGroupKey)
            .setAutoCancel(true)
            .setContentIntent(intent)
            .setColor(iconColor)
            .setColorized(true)
            .setSmallIcon(res_R.mipmap.ic_notification)
    }

    @SuppressLint("MissingPermission")
    override suspend fun makeNewEpisodeNotification(
        animeName: String?,
        airedEpisode: Int?,
        imageUrl: String?
    ) {
        withContext(coroutineContextProvider.ioDispatcher) {
            val noDataString = getString(celebrityRes.string.no_data)
            val episodeAiredString = getString(Res.string.episode_aired)
            val contentText = "$episodeAiredString: ${airedEpisode ?: noDataString}"

            notificationManager?.let { notNullNotificationManager: NotificationManagerCompat ->
                singleBuilder
                    .setContentTitle(animeName ?: noDataString)
                    .setContentText(contentText)
                    .setLargeIcon(createPosterImageBitmap(imageUrl))

                notNullNotificationManager.notify(
                    /* id = */
                    singleId,
                    /* notification = */
                    singleBuilder.build()
                )
                changeSingleIdToNext()

                notNullNotificationManager.notify(
                    /* id = */
                    newEpisodesSummaryId,
                    /* notification = */
                    buildSummaryNotification()
                )
            }
        }
    }

    private suspend fun buildSummaryNotification(): Notification {
        val newEpisodesString = getString(Res.string.new_episodes)
        return NotificationCompat.Builder(
            /* context = */
            appContext,
            /* channelId = */
            CHANNEL_ID
        )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup(newEpisodesGroupKey)
            .setGroupSummary(true)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
            .setColor(iconColor)
            .setColorized(true)
            .setSmallIcon(res_R.mipmap.ic_notification)
            .setStyle(NotificationCompat.InboxStyle().setSummaryText(newEpisodesString))
            .build()
    }

    private suspend fun createPosterImageBitmap(imageUrl: String?): Bitmap? {
        if (imageUrl == null) return null
        return try {
            val result = imageLoader.execute(
                ImageRequest.Builder(appContext).data(imageUrl).build()
            )
            (result as? SuccessResult)?.image?.toBitmap()
        } catch (e: CancellationException) {
            throw e
        } catch (
            // Best-effort poster load for the notification; falling back to no image on any
            // other failure (already logged below) is this method's whole purpose.
            @Suppress("TooGenericExceptionCaught") e: Exception
        ) {
            Log.e(tag, "$e")
            null
        }
    }

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
