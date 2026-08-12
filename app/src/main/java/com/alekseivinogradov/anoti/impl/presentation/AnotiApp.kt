package com.alekseivinogradov.anoti.impl.presentation

import android.app.Application
import android.app.NotificationManager
import com.alekseivinogradov.anoti.impl.presentation.di.AppGraph
import com.alekseivinogradov.anoti.impl.presentation.di.create
import com.alekseivinogradov.anoti.main.impl.presentation.di.MainComponent
import com.alekseivinogradov.anoti.main.impl.presentation.di.MainComponentFactoryHolder

class AnotiApp : Application(), MainComponentFactoryHolder {

    private lateinit var appGraph: AppGraph

    override val mainComponentFactory: MainComponent.Factory
        get() = appGraph.mainComponentFactory

    override fun onCreate() {
        appGraph = AppGraph::class.create(this.applicationContext)
        super.onCreate()

        appGraph.animeBackgroundScheduler.schedulePeriodicUpdate()
        setupAnimeNotificationManager()
    }

    private fun setupAnimeNotificationManager() {
        (getSystemService(NOTIFICATION_SERVICE) as? NotificationManager)
            ?.let { notificationManager: NotificationManager ->
                notificationManager.createNotificationChannel(
                    appGraph.animeNotificationChannelFactory.create()
                )
            }
    }
}
