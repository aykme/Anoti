package com.alekseivinogradov.app.impl.presentation

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.alekseivinogradov.anime_background_update.impl.domain.worker.AnimeUpdateWorker
import com.alekseivinogradov.anime_notification.impl.presentation.factory.AnimeNotificationChannelFactory
import com.alekseivinogradov.app.impl.presentation.di.DaggerAppComponentInternal
import com.alekseivinogradov.di.api.presentation.AnimeBackgroundUpdate
import com.alekseivinogradov.di.api.presentation.app.AppComponent
import com.alekseivinogradov.di.api.presentation.app.ApplicationExternal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnotiApp : Application(), ApplicationExternal {

    override lateinit var appComponent: AppComponent

    @Inject
    @AnimeBackgroundUpdate
    internal lateinit var workManagerConfig: Configuration

    @Inject
    @AnimeBackgroundUpdate
    internal lateinit var animeUpdatePeriodicWork: PeriodicWorkRequest

    @Inject
    internal lateinit var animeNotificationChannelFactory: AnimeNotificationChannelFactory

    override fun onCreate() {
        appComponent = DaggerAppComponentInternal.factory().create(appContext = this)
            .also { it.inject(app = this) }
        super.onCreate()

        setupAnimeUpdateWorkManager()
        setupAnimeNotificationManager()
    }

    private fun setupAnimeUpdateWorkManager() {
        WorkManager.initialize(
            context = this,
            configuration = workManagerConfig
        )
        WorkManager.getInstance(context = this)
            .enqueueUniquePeriodicWork(
                uniqueWorkName = AnimeUpdateWorker.animeUpdatePeriodicWorkName,
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
                request = animeUpdatePeriodicWork
            )
    }

    private fun setupAnimeNotificationManager() {
        (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)
            ?.let { notificationManager: NotificationManager ->
                notificationManager.createNotificationChannel(
                    animeNotificationChannelFactory.create(applicationContext)
                )
            }
    }
}
