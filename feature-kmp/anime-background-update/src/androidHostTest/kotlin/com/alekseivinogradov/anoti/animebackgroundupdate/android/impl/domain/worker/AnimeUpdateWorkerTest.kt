package com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.fake.OtherWorkerFake
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.WorkResult
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.manager.fake.AnimeUpdateManagerFake
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

/**
 * The one other worker in this app, shipped by WorkManager itself. Resolved rather than
 * spelled out, so a rename on its side fails this loudly instead of quietly passing.
 */
private val diagnosticsWorkerClassName: String
    get() = Class.forName("androidx.work.impl.workers.DiagnosticsWorker").name

@RunWith(RobolectricTestRunner::class)
class AnimeUpdateWorkerTest {

    private val appContext: Context = RuntimeEnvironment.getApplication()

    @Test
    fun aPassThatUpdatedEverythingReportsSuccess() = runTest {
        //Given
        val manager = AnimeUpdateManagerFake(result = WorkResult.Success)

        //When
        val result = createWorker(manager).doWork()

        //Then
        assertIs<ListenableWorker.Result.Success>(result)
        assertEquals(1, manager.updateCount)
    }

    @Test
    fun aPassThatCouldNotUpdateEverythingReportsFailureRatherThanAskingForARetry() = runTest {
        //Given
        val manager = AnimeUpdateManagerFake(result = WorkResult.Error)

        //When
        val result = createWorker(manager).doWork()

        //Then
        // A retry would keep the attempt count, and the backoff it drives takes the place of the
        // hourly interval, growing from half a minute to five hours. Failure clears that count
        // for periodic work, so the next pass comes on schedule.
        assertIs<ListenableWorker.Result.Failure>(result)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun aPassThatRanOutOfTimeEndsBeforeThePlatformCanCutItOff() = runTest {
        //Given
        val manager = AnimeUpdateManagerFake(onUpdate = { awaitCancellation() })

        //When
        val result = createWorker(manager).doWork()

        //Then
        // WorkManager stops a worker at ten minutes and a pass cut-off there reports nothing.
        // Ending first is what lets the pages already applied stand.
        assertIs<ListenableWorker.Result.Failure>(result)
        assertTrue(currentTime < 10.minutes.inWholeMilliseconds, "the platform got there first")
        assertTrue(currentTime >= 9.minutes.inWholeMilliseconds, "the pass was cut off early")
    }

    @Test
    fun theFactoryBuildsTheUpdateWorkerItOwns() {
        //Given
        val factory = AnimeUpdateWorker.Factory(AnimeUpdateManagerFake())

        //When
        val worker = TestListenableWorkerBuilder<AnimeUpdateWorker>(appContext)
            .setWorkerFactory(factory)
            .build()

        //Then
        assertIs<AnimeUpdateWorker>(worker)
    }

    @Test
    fun theFactoryLeavesAWorkerItDoesNotOwnToWhoeverDoes() {
        //Given
        val factory = AnimeUpdateWorker.Factory(AnimeUpdateManagerFake())

        //When
        val worker = TestListenableWorkerBuilder<OtherWorkerFake>(appContext)
            .setWorkerFactory(factory)
            .build()

        //Then
        // Answering for a worker this module does not own would hand out the update worker in
        // its place, and WorkManager would never reach the one actually asked for.
        assertIs<OtherWorkerFake>(worker)
    }

    @Test
    fun theFactoryDeclinesTheOneOtherWorkerThisAppShipsWith() {
        //Given
        val factory = AnimeUpdateWorker.Factory(AnimeUpdateManagerFake())

        //When
        val worker = factory.createWorker(
            appContext = appContext,
            workerClassName = diagnosticsWorkerClassName,
            workerParameters = anyWorkerParameters()
        )

        //Then
        // WorkManager ships this one and runs it on a diagnostics broadcast. Claiming it would
        // start an update pass in its place and leave the diagnostics never run.
        assertNull(worker)
    }

    private fun createWorker(manager: AnimeUpdateManagerFake): AnimeUpdateWorker =
        TestListenableWorkerBuilder<AnimeUpdateWorker>(appContext)
            .setWorkerFactory(AnimeUpdateWorker.Factory(manager))
            .build()

    /** A real set of parameters, since the constructor for one is not open to a caller. */
    private fun anyWorkerParameters(): WorkerParameters {
        var captured: WorkerParameters? = null
        TestListenableWorkerBuilder<AnimeUpdateWorker>(appContext)
            .setWorkerFactory(
                object : WorkerFactory() {
                    override fun createWorker(
                        appContext: Context,
                        workerClassName: String,
                        workerParameters: WorkerParameters
                    ): ListenableWorker {
                        captured = workerParameters
                        return AnimeUpdateWorker(
                            appContext = appContext,
                            params = workerParameters,
                            animeUpdateManager = AnimeUpdateManagerFake()
                        )
                    }
                }
            )
            .build()
        return checkNotNull(captured)
    }
}
