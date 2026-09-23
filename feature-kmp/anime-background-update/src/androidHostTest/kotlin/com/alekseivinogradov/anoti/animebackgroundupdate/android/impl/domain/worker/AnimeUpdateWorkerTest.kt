package com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.WorkResult
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.manager.fake.AnimeUpdateManagerFake
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

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
    fun aPassThatCouldNotUpdateEverythingAsksToBeRunAgain() = runTest {
        //Given
        val manager = AnimeUpdateManagerFake(result = WorkResult.Error)

        //When
        val result = createWorker(manager).doWork()

        //Then
        // A page the server did not answer for is worth another go. Reporting failure instead
        // would drop the pass until the next period comes round.
        assertIs<ListenableWorker.Result.Retry>(result)
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

    private fun createWorker(manager: AnimeUpdateManagerFake): AnimeUpdateWorker =
        TestListenableWorkerBuilder<AnimeUpdateWorker>(appContext)
            .setWorkerFactory(AnimeUpdateWorker.Factory(manager))
            .build()
}

/**
 * Stands in for a worker belonging to somebody else. WorkManager builds it by reflection once
 * no factory claims it, which is what makes it visible in the assertion.
 */
internal class OtherWorkerFake(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {
    override fun doWork(): Result = Result.success()
}
