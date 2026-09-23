package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.scheduler

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.WorkResult
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.manager.fake.AnimeUpdateManagerFake
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.scheduler.fake.BackgroundRefreshTaskFake
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

// A handed-over task that is left able to be taken back keeps the platform holding it, so every
// case checks that the pass hands it back as well as what it reported.
private val HANDED_OVER_AND_RELEASED = listOf(true, false)

@OptIn(ExperimentalCoroutinesApi::class)
class BackgroundRefreshPassTest {

    @Test
    fun aPassThatUpdatedEverythingIsReportedAsASuccess() = runTest {
        //Given
        val task = BackgroundRefreshTaskFake()
        val pass = createPass(AnimeUpdateManagerFake())

        //When
        pass.runIn(task)

        //Then
        assertEquals(listOf(true), task.outcomes)
        assertEquals(HANDED_OVER_AND_RELEASED, task.takeBackOffers)
    }

    @Test
    fun aPassThatCouldNotUpdateEverythingIsReportedAsAFailure() = runTest {
        //Given
        val task = BackgroundRefreshTaskFake()
        val pass = createPass(AnimeUpdateManagerFake(result = WorkResult.Error))

        //When
        pass.runIn(task)

        //Then
        // The platform backs the next refresh off after a failure instead of retrying at once.
        assertEquals(listOf(false), task.outcomes)
        assertEquals(HANDED_OVER_AND_RELEASED, task.takeBackOffers)
    }

    @Test
    fun aPassThatThrowsStillEndsTheTask() = runTest {
        //Given
        val task = BackgroundRefreshTaskFake()
        // The handler only keeps a throw from an update pass off the test's own scope. What it
        // stands for in production is a crash, and the task still has to be ended before one.
        val scope = CoroutineScope(
            UnconfinedTestDispatcher(testScheduler) + CoroutineExceptionHandler { _, _ -> }
        )
        val manager = AnimeUpdateManagerFake(onUpdate = { error("the database is gone") })
        val pass = BackgroundRefreshPass(animeUpdateManager = manager, coroutineScope = scope)

        //When
        pass.runIn(task)

        //Then
        assertEquals(listOf(false), task.outcomes)
        assertEquals(HANDED_OVER_AND_RELEASED, task.takeBackOffers)
    }

    @Test
    fun aTaskTakenBackMidPassEndsThePassAndIsToldOnce() = runTest {
        //Given
        val passGate = CompletableDeferred<Unit>()
        val manager = AnimeUpdateManagerFake(onUpdate = { passGate.await() })
        val task = BackgroundRefreshTaskFake()
        createPass(manager).runIn(task)

        //When
        task.expire()
        passGate.complete(Unit)

        //Then
        // Telling the platform twice is what it treats as a programming error.
        assertEquals(listOf(false), task.outcomes)
        assertEquals(HANDED_OVER_AND_RELEASED, task.takeBackOffers)
    }

    @Test
    fun aPassThatCannotEvenStartStillEndsTheTask() = runTest {
        //Given
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        scope.cancel()
        val manager = AnimeUpdateManagerFake()
        val task = BackgroundRefreshTaskFake()
        val pass = BackgroundRefreshPass(animeUpdateManager = manager, coroutineScope = scope)

        //When
        pass.runIn(task)

        //Then
        // A task left unended costs the app every background refresh after it.
        assertEquals(listOf(false), task.outcomes)
        assertEquals(HANDED_OVER_AND_RELEASED, task.takeBackOffers)
        assertEquals(0, manager.updateCount)
    }

    private fun TestScope.createPass(manager: AnimeUpdateManagerFake) = BackgroundRefreshPass(
        animeUpdateManager = manager,
        coroutineScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
    )
}
