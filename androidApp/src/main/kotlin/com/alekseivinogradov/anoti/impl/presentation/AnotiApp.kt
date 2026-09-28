package com.alekseivinogradov.anoti.impl.presentation

import android.app.Application
import android.app.NotificationManager
import androidx.work.Configuration
import com.alekseivinogradov.anoti.di.kmp.DiAppComponent
import com.alekseivinogradov.anoti.di.kmp.create
import com.alekseivinogradov.anoti.main.impl.di.DiRootComponent
import com.alekseivinogradov.anoti.main.impl.di.create
import com.alekseivinogradov.anoti.main.impl.presentation.di.DiRootComponentHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnotiApp : Application(), DiRootComponentHolder, Configuration.Provider {

    private lateinit var diAppComponent: DiAppComponent

    override val workManagerConfiguration: Configuration
        get() = diAppComponent.workManagerConfiguration

    override fun createDiRootComponent(): DiRootComponent =
        DiRootComponent::class.create(diAppComponent)

    override fun onCreate() {
        diAppComponent = DiAppComponent::class.create(this.applicationContext)
        super.onCreate()

        CoroutineScope(diAppComponent.coroutineContextProvider.appMainCoroutineContext).launch {
            startUp()
        }
    }

    /** What the app arranges once per process, off the path to its first screen. */
    internal suspend fun startUp() {
        // None of this draws anything, and all of it costs frames on the way to the first screen.
        // The channel's strings are read off disk. The first WorkManager handle opens a database.
        withContext(diAppComponent.coroutineContextProvider.ioDispatcher) {
            // The worker posts into this channel, so it must exist before the work is enqueued.
            setupAnimeNotificationManager()
            diAppComponent.animeBackgroundScheduler.schedulePeriodicUpdate()
        }
    }

    private suspend fun setupAnimeNotificationManager() {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            diAppComponent.animeNotificationChannelFactory.create()
        )
    }
}
