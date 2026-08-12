package com.alekseivinogradov.anoti.animenotification.android.impl.presentation.manager

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.alekseivinogradov.anoti.animenotification.android.impl.presentation.factory.CHANNEL_ID
import com.alekseivinogradov.anoti.animenotification.external.android.impl.presentation.provider.AnimeNotificationIntentProvider
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.episode_aired
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.new_episodes
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.no_data
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import kotlin.coroutines.cancellation.CancellationException
import com.alekseivinogradov.anoti.celebrity.kmp.R as res_R
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res as celebrityRes

class AnimeNotificationManagerImpl(
    appContext: Context,
    animeNotificationIntentProvider: AnimeNotificationIntentProvider,
    coroutineContextProvider: CoroutineContextProvider
) : AnimeNotificationManager {
    private val tag = "ANIME_NOTIFICATION_MANAGER"

    private val episodeAiredString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(Res.string.episode_aired)
        }

    private val noDataString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(celebrityRes.string.no_data)
        }

    private val iconColor: Int =
        appContext.resources.getColor(
            /* id = */
            res_R.color.silver_transparent,
            /* theme = */
            appContext.resources.newTheme()
        )

    private var glideRequestManager: RequestManager? = null
    private var singleBuilder: NotificationCompat.Builder
    private var intent: PendingIntent? = null
    private var summaryNotification: Notification
    private var notificationManager: NotificationManagerCompat? = null

    private val summaryNewEpisodesStyle = NotificationCompat.InboxStyle()
    private val newEpisodesGroupKey = "ANIME_NOTIFICATION_NEW_EPISODE_GROUP_KEY"

    /** Single id should be from [DEFAULT_SINGLE_ID] to [MAX_SINGLE_ID] */
    private var singleId = DEFAULT_SINGLE_ID

    /** Group ids should be from 0 to 9 */
    private val newEpisodesSummaryId = 0

    init {
        glideRequestManager = Glide.with(appContext)
        intent = animeNotificationIntentProvider.getNewEpisodeNotificationIntent(appContext)
        notificationManager = NotificationManagerCompat.from(appContext)

        val newEpisodesString =
            runBlocking(coroutineContextProvider.ioDispatcher) {
                getString(Res.string.new_episodes)
            }

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

        summaryNotification = NotificationCompat.Builder(
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
            .setStyle(summaryNewEpisodesStyle.setSummaryText(newEpisodesString))
            .build()
    }

    @SuppressLint("MissingPermission")
    override fun makeNewEpisodeNotification(
        animeName: String?,
        airedEpisode: Int?,
        imageUrl: String?
    ) {
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
                summaryNotification
            )
        }
    }

    private fun createPosterImageBitmap(imageUrl: String?): Bitmap? {
        return try {
            glideRequestManager
                ?.asBitmap()
                ?.load(imageUrl)
                ?.submit()
                ?.get()
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
