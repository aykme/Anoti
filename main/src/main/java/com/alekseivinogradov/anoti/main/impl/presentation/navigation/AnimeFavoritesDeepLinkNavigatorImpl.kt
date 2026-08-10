package com.alekseivinogradov.anoti.main.impl.presentation.navigation

import android.app.PendingIntent
import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import com.alekseivinogradov.anoti.main.R as main_R
import com.alekseivinogradov.anoti.main.impl.presentation.MainActivity
import com.alekseivinogradov.anoti.navigation.platform.api.presentation.AnimeFavoritesDeepLinkNavigator
import javax.inject.Inject

class AnimeFavoritesDeepLinkNavigatorImpl @Inject constructor() : AnimeFavoritesDeepLinkNavigator {
    override fun getAnimeFavoritesDeepLinkIntent(appContext: Context): PendingIntent {
        return NavDeepLinkBuilder(appContext)
            .setComponentName(MainActivity::class.java)
            .setGraph(main_R.navigation.nav_graph)
            .setDestination(main_R.id.anime_favorites)
            .createPendingIntent()
    }
}
