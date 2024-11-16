package com.alekseivinogradov.anime_notification.impl.presentation.manager

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.alekseivinogradov.anime_notification.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anime_notification.impl.presentation.factory.AnimeNotificationChannelFactory
import com.alekseivinogradov.anime_notification.impl.presentation.provider.AnimeNotificationIntentProvider
import com.alekseivinogradov.anime_notification_platform.R
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import kotlin.coroutines.cancellation.CancellationException
import com.alekseivinogradov.res.R as res_R

class AnimeNotificationManagerImpl(
    appContext: Context,
    private val animeNotificationIntentProvider: AnimeNotificationIntentProvider
) : AnimeNotificationManager {
    private val tag = "ANIME_NOTIFICATION_MANAGER"

    /**
     * Most of the parameters are initialized in the "init" block,
     * so as not to save "context" as a property.
     */

    private var resources: Resources? = null

    private val episodeAiredString: String
        get() = resources?.getString(R.string.episode_aired) ?: ""

    private val noDataString: String
        get() = resources?.getString(R.string.no_data) ?: ""

    private val iconColor: Int?
        get() = resources?.getColor(
            /* id = */ res_R.color.silver_transpaent,
            /* theme = */ resources?.newTheme()
        )

    private val newEpisodesString: String
        get() = resources?.getString(R.string.new_episodes) ?: ""

    private var glideRequestManager: RequestManager? = null
    private var singleBuilder: NotificationCompat.Builder? = null
    private var intent: PendingIntent? = null
    private var summaryNotification: Notification? = null
    private var notificationManager: NotificationManagerCompat? = null

    private val summaryNewEpisodesStyle = NotificationCompat.InboxStyle()
    private val newEpisodesGroupKey = "ANIME_NOTIFICATION_NEW_EPISODE_GROUP_KEY"

    /** Single id should be from "defaultSingleId" to "maxSingleId" */
    private val defaultSingleId = 10
    private val maxSingleId = 99
    private var singleId = defaultSingleId

    /** Group ids should be from 0 to 9 */
    private val newEpisodesSummaryId = 0

    init {
        /**
         * These operations are performed in the "init block"
         * so as not to save the "context" as a variable.
         */
        resources = appContext.resources
        glideRequestManager = Glide.with(appContext)
        intent = animeNotificationIntentProvider.getNewEpisodeNotificationIntent(appContext)

        singleBuilder = iconColor?.let { notNullIcon: Int ->
            NotificationCompat.Builder(
                /* context = */ appContext,
                /* channelId = */ AnimeNotificationChannelFactory.channelId
            )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setGroup(newEpisodesGroupKey)
                .setAutoCancel(true)
                .setContentIntent(intent)
                .setColor(notNullIcon)
                .setColorized(true)
                .setSmallIcon(res_R.mipmap.ic_notification)
        }

        summaryNotification = iconColor?.let { notNullIcon: Int ->
            NotificationCompat.Builder(
                /* context = */ appContext,
                /* channelId = */ AnimeNotificationChannelFactory.channelId
            )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setGroup(newEpisodesGroupKey)
                .setGroupSummary(true)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                .setColor(notNullIcon)
                .setColorized(true)
                .setSmallIcon(res_R.mipmap.ic_notification)
                .setStyle(summaryNewEpisodesStyle.setSummaryText(newEpisodesString))
                .build()
        }



        notificationManager = NotificationManagerCompat.from(appContext)
    }

    @SuppressLint("MissingPermission")
    override fun makeNewEpisodeNotification(
        animeName: String?,
        airedEpisode: Int?,
        imageUrl: String?
    ) {
        val contentText = "${episodeAiredString}: ${airedEpisode ?: noDataString}"

        notificationManager?.let { notNullNotificationManager: NotificationManagerCompat ->
            singleBuilder?.let { notNullSingleBuilder: NotificationCompat.Builder ->
                notNullSingleBuilder
                    .setContentTitle(animeName ?: noDataString)
                    .setContentText(contentText)
                    .setLargeIcon(createPosterImageBitmap(imageUrl))

                notNullNotificationManager.notify(
                    /* id = */ singleId,
                    /* notification = */ notNullSingleBuilder.build()
                )
                changeSingleIdToNext()

                summaryNotification?.let { notNullSummaryNotification: Notification ->
                    notNullNotificationManager.notify(
                        /* id = */ newEpisodesSummaryId,
                        /* notification = */ notNullSummaryNotification
                    )
                }
            }
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
        } catch (e: Exception) {
            Log.e(tag, "$e")
            null
        }
    }

    private fun changeSingleIdToNext() {
        if (singleId < maxSingleId) {
            singleId++
        } else {
            singleId = defaultSingleId
        }
    }
}
