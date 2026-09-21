package com.alekseivinogradov.anoti.impl.presentation

import android.app.NotificationManager
import android.content.Context
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.ANIME_UPDATE_PERIODIC_WORK_NAME
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.AnimeUpdateWorker
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anoti.animenotification.android.impl.presentation.factory.CHANNEL_ID
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

/**
 * Drives [AnotiApp.startUp] directly. Robolectric creates the application, so `onCreate` has
 * already queued the same call on the main looper. That message is never let through, which
 * leaves the test as the only caller.
 */
@RunWith(RobolectricTestRunner::class)
class AnotiAppTest {

    private lateinit var app: AnotiApp

    @BeforeTest
    fun setUp() {
        app = checkNotNull(RuntimeEnvironment.getApplication() as? AnotiApp)
        // Built from scratch rather than copied from the graph. A copied configuration carries a
        // task executor, and the helper then leaves WorkManager on real threads instead of
        // running it on the calling one.
        WorkManagerTestInitHelper.initializeTestWorkManager(
            /* context = */
            app,
            /* configuration = */
            Configuration.Builder().setWorkerFactory(DoNothingWorkerFactory()).build()
        )
    }

    @AfterTest
    fun tearDown() {
        // Without this the connections WorkManager opened are still held when the test ends, and
        // the platform reports each one as leaked.
        WorkManagerTestInitHelper.closeWorkDatabase()
    }

    @Test
    fun theNotificationChannelExistsOnceStartupHasRun() = runTest {
        //Given
        val notificationManager = app.getSystemService(NotificationManager::class.java)

        //When
        app.startUp()

        //Then
        val channel = assertNotNull(notificationManager.getNotificationChannel(CHANNEL_ID))
        assertEquals(NotificationManager.IMPORTANCE_DEFAULT, channel.importance)
        assertTrue(channel.shouldVibrate())
        assertTrue(channel.name.isNotBlank())
        val description = assertNotNull(channel.description)
        assertTrue(description.isNotBlank())
    }

    @Test
    fun theBackgroundUpdateIsScheduledOnceStartupHasRun() = runTest {
        //Given
        val workManager = WorkManager.getInstance(app)

        //When
        app.startUp()

        //Then
        val scheduled = workManager
            .getWorkInfosForUniqueWork(ANIME_UPDATE_PERIODIC_WORK_NAME)
            .get()
        assertEquals(1, scheduled.size)
        // The repeat interval is what makes this the hourly check rather than a one-off.
        assertEquals(
            AnimeUpdateManager.DEFAULT_ANIME_UPDATE_INTERVAL_MINUTES,
            scheduled.single().periodicityInfo?.repeatIntervalMillis?.milliseconds?.inWholeMinutes
        )
    }

    @Test
    fun aSecondStartupLeavesTheAlreadyScheduledUpdateAlone() = runTest {
        //Given
        val workManager = WorkManager.getInstance(app)
        app.startUp()
        val firstId = workManager
            .getWorkInfosForUniqueWork(ANIME_UPDATE_PERIODIC_WORK_NAME)
            .get()
            .single()
            .id

        //When
        app.startUp()

        //Then
        val scheduled = workManager
            .getWorkInfosForUniqueWork(ANIME_UPDATE_PERIODIC_WORK_NAME)
            .get()
        assertEquals(1, scheduled.size)
        assertEquals(firstId, scheduled.single().id)
    }

    @Test
    fun workManagerGetsAConfigurationThatCanBuildTheUpdateWorker() {
        //Given
        val configuration = app.workManagerConfiguration

        //When
        val workerFactory = configuration.workerFactory

        //Then
        assertEquals(AnimeUpdateWorker.Factory::class, workerFactory::class)
        // The worker takes its update manager as a third constructor argument, so building it is
        // the other half of the check.
        TestListenableWorkerBuilder<AnimeUpdateWorker>(app)
            .setWorkerFactory(workerFactory)
            .build()
    }

    @Test
    fun startupOpensNoDatabaseOfItsOwn() = runTest {
        //Given
        app.createDiRootComponent()

        //When
        app.startUp()

        //Then
        // Opening the anime database is disk work, and nothing on the way to the first screen
        // needs it. WorkManager keeps its own database out of this list.
        assertContentEquals(emptyArray(), app.databaseList())
    }

    @Test
    fun everyScreenHostGetsItsOwnRootComponent() {
        //Given
        val first = app.createDiRootComponent()

        //When
        val second = app.createDiRootComponent()

        //Then
        assertNotSame(first, second)
    }
}

/**
 * Stands in for the app's own factory while WorkManager is under test. Scheduling is what these
 * tests exercise; running the update is not, and a worker that did would reach the network.
 */
private class DoNothingWorkerFactory : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = object : Worker(appContext, workerParameters) {
        override fun doWork(): Result = Result.success()
    }
}
