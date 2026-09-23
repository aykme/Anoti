package com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker

import android.content.Context
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.WorkResult
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.manager.fake.AnimeUpdateManagerFake
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

/** Far enough past the enqueue to tell a rescheduled pass from the one just enqueued. */
private const val SETTLED_MARGIN_MILLIS = 10_000L

/**
 * What WorkManager does with the result a failed pass reports. The worker's own answer is half
 * the behavior; the half deciding when the next pass runs lives in the library, so a test of
 * the worker on its own cannot see it.
 *
 * Each case waits on WorkManager's own stream of work info rather than on a clock, so the wait
 * ends when the library says the pass is over.
 */
// One function per case under test, plus the helpers those cases share.
@Suppress("TooManyFunctions")
@RunWith(RobolectricTestRunner::class)
class AnimeUpdateWorkSchedulingTest {

    private val appContext: Context = RuntimeEnvironment.getApplication()

    private lateinit var workManager: WorkManager

    @BeforeTest
    fun setUp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(
            /* context = */
            appContext,
            /* configuration = */
            Configuration.Builder()
                .setWorkerFactory(
                    AnimeUpdateWorker.Factory(AnimeUpdateManagerFake(result = WorkResult.Error))
                )
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
    fun aFailedHourlyPassStillWaitsAnHourForTheNextOne() = runTest {
        //Given
        val request = periodicRequest()
        val enqueuedAt = System.currentTimeMillis()
        enqueuePeriodic(request)

        //When
        letTheWorkRun(request.id)
        val rescheduled = workInfoFor(request.id) { info: WorkInfo ->
            info.state == WorkInfo.State.ENQUEUED &&
                info.nextScheduleTimeMillis > enqueuedAt + SETTLED_MARGIN_MILLIS
        }

        //Then
        // Asking for a retry instead of reporting failure leaves the attempt count standing, and
        // backoff then takes the place of the interval: the next pass comes in half a minute,
        // doubling each time up to five hours.
        val waitMillis = rescheduled.nextScheduleTimeMillis - enqueuedAt
        assertTrue(
            waitMillis > 30.minutes.inWholeMilliseconds,
            "the hourly interval gave way to backoff: next pass in ${waitMillis}ms"
        )
    }

    @Test
    fun aFailedRefreshLetsTheNextPullStartAFreshPass() = runTest {
        //Given
        // The screen keeps one of these, and it keeps one request, so every pull enqueues the
        // same work rather than a new piece of it.
        val request = oneOffRequest()
        enqueueOneOff(request)
        letTheWorkRun(request.id)
        workInfoFor(request.id) { info: WorkInfo ->
            info.state.isFinished || info.runAttemptCount > 0
        }

        //When
        enqueueOneOff(request)

        //Then
        // KEEP declines while the previous work is still enqueued or running, so a pass left
        // waiting in backoff would swallow every pull until that backoff ran out. A pass that
        // ended is replaced instead, and the replacement starts its attempts from nothing.
        val accepted = workInfoFor(request.id) { info: WorkInfo ->
            info.state == WorkInfo.State.ENQUEUED
        }
        assertEquals(
            0,
            accepted.runAttemptCount,
            "the pull was dropped onto the pass that had already failed"
        )
    }

    @Test
    fun aRefreshInBetweenLeavesTheHourlySchedulePlanned() = runTest {
        //Given
        // The two are separate work, and the refresh one ends for good once it has run. The
        // hourly one has to be standing afterwards, or the app stops updating on its own.
        val periodic = periodicRequest()
        enqueuePeriodic(periodic)
        letTheWorkRun(periodic.id)
        val afterFirstHourlyPass = workInfoFor(periodic.id) { info: WorkInfo ->
            info.state == WorkInfo.State.ENQUEUED && info.runAttemptCount == 0
        }

        //When
        val refresh = oneOffRequest()
        enqueueOneOff(refresh)
        letTheWorkRun(refresh.id)
        workInfoFor(refresh.id) { info: WorkInfo -> info.state.isFinished }

        //Then
        val hourly = workInfoFor(periodic.id) { info: WorkInfo ->
            info.state == WorkInfo.State.ENQUEUED
        }
        assertEquals(0, hourly.runAttemptCount, "the refresh pushed the hourly pass into backoff")
        assertEquals(
            afterFirstHourlyPass.nextScheduleTimeMillis,
            hourly.nextScheduleTimeMillis,
            "the refresh moved when the next hourly pass runs"
        )
    }

    private fun periodicRequest() = PeriodicWorkRequestBuilder<AnimeUpdateWorker>(
        repeatInterval = AnimeUpdateManager.DEFAULT_ANIME_UPDATE_INTERVAL_MINUTES,
        repeatIntervalTimeUnit = TimeUnit.MINUTES
    ).setConstraints(ANIME_UPDATE_WORK_CONSTRAINTS).build()

    private fun enqueuePeriodic(request: PeriodicWorkRequest) {
        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName = ANIME_UPDATE_PERIODIC_WORK_NAME,
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
            request = request
        )
    }

    private fun oneOffRequest() = OneTimeWorkRequestBuilder<AnimeUpdateWorker>()
        .setConstraints(ANIME_UPDATE_WORK_CONSTRAINTS)
        .build()

    private fun enqueueOneOff(request: OneTimeWorkRequest) {
        workManager.enqueueUniqueWork(
            uniqueWorkName = ANIME_UPDATE_ONCE_WORK_NAME,
            existingWorkPolicy = ExistingWorkPolicy.KEEP,
            request = request
        )
    }

    /** Lets the work past the constraints it is waiting on, so the pass actually runs. */
    private fun letTheWorkRun(id: UUID) {
        checkNotNull(WorkManagerTestInitHelper.getTestDriver(appContext))
            .setAllConstraintsMet(id)
    }

    private suspend fun workInfoFor(id: UUID, until: (WorkInfo) -> Boolean): WorkInfo =
        workManager.getWorkInfoByIdFlow(id).filterNotNull().first(until)
}
