package com.alekseivinogradov.anoti.impl.presentation

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.animeUpdatePeriodicWorkName
import com.alekseivinogradov.anoti.animenotification.android.impl.presentation.factory.AnimeNotificationChannelFactory
import com.alekseivinogradov.anoti.impl.presentation.di.DaggerAppComponentInternal
import com.alekseivinogradov.anoti.di.platform.api.presentation.AnimeBackgroundUpdate
import com.alekseivinogradov.anoti.di.platform.api.presentation.app.AppComponent
import com.alekseivinogradov.anoti.di.platform.api.presentation.app.ApplicationExternal
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
        appComponent = DaggerAppComponentInternal.factory().create(
            appContext = this.applicationContext
        ).also { it.inject(app = this) }
        super.onCreate()

        setupAnimeUpdateWorkManager()
        setupAnimeNotificationManager()
    }

    private fun setupAnimeUpdateWorkManager() {
        WorkManager.initialize(
            context = this.applicationContext,
            configuration = workManagerConfig
        )
        WorkManager.getInstance(context = this.applicationContext)
            .enqueueUniquePeriodicWork(
                uniqueWorkName = animeUpdatePeriodicWorkName,
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
