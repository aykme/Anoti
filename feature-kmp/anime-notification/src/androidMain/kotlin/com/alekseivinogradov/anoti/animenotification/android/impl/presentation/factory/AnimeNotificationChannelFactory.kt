package com.alekseivinogradov.anoti.animenotification.android.impl.presentation.factory

import android.app.NotificationChannel
import android.app.NotificationManager
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.anime_notification_channel
import com.alekseivinogradov.anoti.animenotification.kmp.generated.resources.anime_notification_channel_description
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import kotlinx.coroutines.runBlocking
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.getString
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * Builds the app's anime notification channel. App-scoped: the channel is registered once, at
 * `Application.onCreate` time.
 *
 * @param coroutineContextProvider supplies the dispatcher the channel's localized strings are
 *   read on.
 */
@SingleIn(AppScope::class)
class AnimeNotificationChannelFactory @Inject constructor(
    private val coroutineContextProvider: CoroutineContextProvider
) {

    fun create(): NotificationChannel {
        val name = runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(Res.string.anime_notification_channel)
        }
        val channelDescription = runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(Res.string.anime_notification_channel_description)
        }
        return NotificationChannel(
            /* id = */
            CHANNEL_ID,
            /* name = */
            name,
            /* importance = */
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = channelDescription
            enableVibration(true)
        }
    }
}

const val CHANNEL_ID = "ANIME_NOTIFICATION_CHANNEL_ID"
