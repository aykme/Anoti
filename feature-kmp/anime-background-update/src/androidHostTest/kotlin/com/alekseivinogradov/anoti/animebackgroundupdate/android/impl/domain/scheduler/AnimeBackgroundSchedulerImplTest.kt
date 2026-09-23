package com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.scheduler

import android.content.Context
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.ANIME_UPDATE_PERIODIC_WORK_NAME
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.ANIME_UPDATE_WORK_CONSTRAINTS
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.AnimeUpdateWorker
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.manager.fake.AnimeUpdateManagerFake
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val OTHER_INTERVAL_MINUTES = 30L
private const val REPEATED_APP_STARTS = 5

@RunWith(RobolectricTestRunner::class)
class AnimeBackgroundSchedulerImplTest {

    private val appContext: Context = RuntimeEnvironment.getApplication()

    private lateinit var workManager: WorkManager

    @BeforeTest
    fun setUp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(
            /* context = */
            appContext,
            /* configuration = */
            Configuration.Builder()
                .setWorkerFactory(AnimeUpdateWorker.Factory(AnimeUpdateManagerFake()))
                .build()
        )
        workManager = WorkManager.getInstance(appContext)
    }

    @AfterTest
    fun tearDown() {
        // Without this the connections WorkManager opened are still held when the test ends, and
        // the platform reports each one as leaked.
        WorkManagerTestInitHelper.closeWorkDatabase()
    }

    @Test
    fun theUpdateIsScheduledUnderTheNameTheRestOfTheAppLooksItUpBy() {
        //Given
        val scheduler = createScheduler(periodicWork())

        //When
        scheduler.schedulePeriodicUpdate()

        //Then
        assertEquals(1, scheduledWork().size)
    }

    @Test
    fun theRequestItWasGivenIsTheOneThatGetsScheduled() {
        //Given
        // Deliberately not the constraint production uses: a scheduler building a request of
        // its own would still look right against that one.
        val distinctConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()
        val scheduler = createScheduler(
            PeriodicWorkRequestBuilder<AnimeUpdateWorker>(
                repeatInterval = OTHER_INTERVAL_MINUTES,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            ).setConstraints(distinctConstraints).build()
        )

        //When
        scheduler.schedulePeriodicUpdate()

        //Then
        assertEquals(
            NetworkType.UNMETERED,
            scheduledWork().single().constraints.requiredNetworkType
        )
    }

    @Test
    fun rescheduleWithoutAChangeLeavesTheRunningScheduleAlone() {
        //Given
        val scheduler = createScheduler(periodicWork())
        scheduler.schedulePeriodicUpdate()
        val firstId = scheduledWork().single().id

        //When
        scheduler.schedulePeriodicUpdate()

        //Then
        assertEquals(1, scheduledWork().size)
        assertEquals(firstId, scheduledWork().single().id)
    }

    @Test
    fun openingTheAppAgainDoesNotPushTheNextPassBack() {
        //Given
        // Every process start schedules again, so a phone the app is opened on often would
        // never reach its next pass if each one moved the schedule along.
        val scheduler = createScheduler(periodicWork())
        scheduler.schedulePeriodicUpdate()
        val firstNextRun = scheduledWork().single().nextScheduleTimeMillis

        //When
        repeat(REPEATED_APP_STARTS) { scheduler.schedulePeriodicUpdate() }

        //Then
        assertEquals(1, scheduledWork().size)
        assertEquals(
            firstNextRun,
            scheduledWork().single().nextScheduleTimeMillis,
            "the hourly pass was pushed further away by opening the app"
        )
    }

    @Test
    fun aChangedRequestTakesOverTheScheduleAlreadyRunning() {
        //Given
        createScheduler(periodicWork()).schedulePeriodicUpdate()
        val firstId = scheduledWork().single().id

        //When
        createScheduler(periodicWork(OTHER_INTERVAL_MINUTES)).schedulePeriodicUpdate()

        //Then
        // An app already on a phone carries whatever schedule its previous version enqueued.
        // Keeping that one would pin it to the old interval and the old constraints for good.
        // The schedule is rewritten in place, so the work keeps its identity and its next run
        // is not pushed back by a fresh enqueue.
        assertEquals(1, scheduledWork().size)
        assertEquals(firstId, scheduledWork().single().id)
        assertEquals(
            OTHER_INTERVAL_MINUTES,
            scheduledWork().single().periodicityInfo?.repeatIntervalMillis
                ?.let { TimeUnit.MILLISECONDS.toMinutes(it) }
        )
    }

    private fun createScheduler(request: PeriodicWorkRequest) = AnimeBackgroundSchedulerImpl(
        workManager = { workManager },
        animeUpdatePeriodicWork = request
    )

    private fun periodicWork(intervalMinutes: Long = OTHER_INTERVAL_MINUTES * 2) =
        PeriodicWorkRequestBuilder<AnimeUpdateWorker>(
            repeatInterval = intervalMinutes,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        ).setConstraints(ANIME_UPDATE_WORK_CONSTRAINTS).build()

    private fun scheduledWork(): List<WorkInfo> = workManager
        .getWorkInfosForUniqueWork(ANIME_UPDATE_PERIODIC_WORK_NAME)
        .get()
}
