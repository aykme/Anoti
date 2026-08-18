package com.alekseivinogradov.anoti.impl.presentation

import android.app.Application
import android.app.NotificationManager
import com.alekseivinogradov.anoti.di.kmp.DiAppComponent
import com.alekseivinogradov.anoti.di.kmp.create
import com.alekseivinogradov.anoti.main.impl.di.DiRootComponent
import com.alekseivinogradov.anoti.main.impl.di.create
import com.alekseivinogradov.anoti.main.impl.presentation.di.DiRootComponentHolder

class AnotiApp : Application(), DiRootComponentHolder {

    private lateinit var diAppComponent: DiAppComponent

    override fun createDiRootComponent(): DiRootComponent =
        DiRootComponent::class.create(diAppComponent)

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
