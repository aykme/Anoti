package com.alekseivinogradov.anoti.impl.presentation

import android.app.Application
import android.app.NotificationManager
import com.alekseivinogradov.anoti.impl.presentation.di.DiAppComponent
import com.alekseivinogradov.anoti.impl.presentation.di.create
import com.alekseivinogradov.anoti.main.impl.presentation.di.DiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.di.DiRootComponentHolder

class AnotiApp : Application(), DiRootComponentHolder {

    private lateinit var diAppComponent: DiAppComponent

    override val diRootComponentFactory: DiRootComponent.Factory
        get() = diAppComponent.diRootComponentFactory

    override fun onCreate() {
        diAppComponent = DiAppComponent::class.create(this.applicationContext)
        super.onCreate()

        diAppComponent.animeBackgroundScheduler.schedulePeriodicUpdate()
        setupAnimeNotificationManager()
    }

    private fun setupAnimeNotificationManager() {
        (getSystemService(NOTIFICATION_SERVICE) as? NotificationManager)
            ?.let { notificationManager: NotificationManager ->
                notificationManager.createNotificationChannel(
                    diAppComponent.animeNotificationChannelFactory.create()
                )
            }
    }
}
