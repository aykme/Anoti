package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.scheduler

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val RACING_COMPLETIONS = 50

class OneShotCompletionTest {

    @Test
    fun theFirstOutcomeIsReported() {
        //Given
        val reported = mutableListOf<Boolean>()
        val completion = OneShotCompletion { reported += it }

        //When
        completion.complete(success = true)

        //Then
        assertEquals(listOf(true), reported)
    }

    @Test
    fun anOutcomeArrivingAfterTheFirstIsDropped() {
        //Given
        val reported = mutableListOf<Boolean>()
        val completion = OneShotCompletion { reported += it }
        completion.complete(success = true)

        //When
        completion.complete(success = false)

        //Then
        // The pass finishing and the platform expiring the task can land together. Reporting
        // twice is what the platform treats as a programming error.
        assertEquals(listOf(true), reported)
    }

    @Test
    fun outcomesArrivingAtOnceStillReportOnlyOne() = runTest {
        //Given
        val reported = mutableListOf<Boolean>()
        val completion = OneShotCompletion { reported += it }

        //When
        (1..RACING_COMPLETIONS)
            .map { attempt: Int -> async { completion.complete(success = attempt % 2 == 0) } }
            .awaitAll()

        //Then
        assertEquals(1, reported.size)
    }
}
