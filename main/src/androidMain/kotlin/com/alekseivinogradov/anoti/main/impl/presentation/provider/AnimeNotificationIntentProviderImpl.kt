package com.alekseivinogradov.anoti.main.impl.presentation.provider

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.alekseivinogradov.anoti.animenotification.external.android.impl.presentation.provider.AnimeNotificationIntentProvider
import com.alekseivinogradov.anoti.main.impl.presentation.MainActivity
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

/**
 * The "main"-side implementation of [AnimeNotificationIntentProvider]. Lives here because it is
 * the only module that owns both the root navigation component and the notification's target
 * activity.
 *
 * `FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK` guarantees this always goes through
 * [MainActivity.onCreate] — never `onNewIntent` — whether the app was already running or not;
 * `FLAG_ACTIVITY_CLEAR_TASK` specifically is what forces the existing task to be torn down first.
 */
@Inject
class AnimeNotificationIntentProviderImpl : AnimeNotificationIntentProvider {
    override fun getNewEpisodeNotificationIntent(appContext: Context): PendingIntent {
        val intent = Intent(appContext, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra(
                MainActivity.EXTRA_DEEP_LINK_TARGET,
                Json.encodeToString(NavRootConfig.serializer(), NavRootConfig.AnimeFavorites)
            )
        }
        return PendingIntent.getActivity(
            /* context = */
            appContext,
            /* requestCode = */
            REQUEST_CODE,
            /* intent = */
            intent,
            /* flags = */
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private companion object {
        const val REQUEST_CODE = 0
    }
}
