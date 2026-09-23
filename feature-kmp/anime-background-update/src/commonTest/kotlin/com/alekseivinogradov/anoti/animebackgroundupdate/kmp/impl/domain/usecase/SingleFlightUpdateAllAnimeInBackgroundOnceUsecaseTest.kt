package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.manager.fake.AnimeUpdateManagerFake
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SingleFlightUpdateAllAnimeInBackgroundOnceUsecaseTest {

    @Test
    fun askingForAnUpdateRunsOnePass() = runTest {
        //Given
        val manager = AnimeUpdateManagerFake()
        val usecase = createUsecase(manager)

        //When
        usecase.execute()

        //Then
        assertEquals(1, manager.updateCount)
    }

    @Test
    fun askingAgainWhileAPassIsStillRunningStartsNoSecondOne() = runTest {
        //Given
        val passGate = CompletableDeferred<Unit>()
        val manager = AnimeUpdateManagerFake(onUpdate = { passGate.await() })
        val usecase = createUsecase(manager)
        usecase.execute()

        //When
        usecase.execute()
        passGate.complete(Unit)

        //Then
        // Two passes over the same library would fetch everything twice and could notify twice
        // about one episode.
        assertEquals(1, manager.updateCount)
    }

    @Test
    fun aNewPassCanStartOnceTheLastOneHasFinished() = runTest {
        //Given
        val manager = AnimeUpdateManagerFake()
        val usecase = createUsecase(manager)
        usecase.execute()

        //When
        usecase.execute()

        //Then
        assertEquals(2, manager.updateCount)
    }

    private fun TestScope.createUsecase(
        manager: AnimeUpdateManagerFake
    ) = SingleFlightUpdateAllAnimeInBackgroundOnceUsecase(
        animeUpdateManager = manager,
        coroutineScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
    )
}
