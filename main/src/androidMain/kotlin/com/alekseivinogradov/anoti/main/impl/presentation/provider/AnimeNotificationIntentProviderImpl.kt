package com.alekseivinogradov.anoti.main.impl.presentation.provider

import android.app.PendingIntent
import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import com.alekseivinogradov.anoti.animenotification.external.android.impl.presentation.provider.AnimeNotificationIntentProvider
import com.alekseivinogradov.anoti.di.kmp.scope.AppScope
import com.alekseivinogradov.anoti.main.impl.presentation.MainActivity
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import com.alekseivinogradov.anoti.main.R as main_R
import com.alekseivinogradov.anoti.navigation.R as navigation_R

/**
 * The "main"-side implementation of [AnimeNotificationIntentProvider]. Lives here because it is
 * the only module that owns both the navigation graph and the notification's target activity.
 */
@Inject
@ContributesBinding(AppScope::class)
class AnimeNotificationIntentProviderImpl : AnimeNotificationIntentProvider {
    override fun getNewEpisodeNotificationIntent(appContext: Context): PendingIntent {
        return NavDeepLinkBuilder(appContext)
            .setComponentName(MainActivity::class.java)
            .setGraph(navigation_R.navigation.nav_graph)
            .setDestination(main_R.id.anime_favorites)
            .createPendingIntent()
    }
}
