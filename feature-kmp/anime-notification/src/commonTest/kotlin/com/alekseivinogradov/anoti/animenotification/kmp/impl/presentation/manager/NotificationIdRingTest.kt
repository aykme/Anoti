package com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.manager

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

// One function per case under test, plus the helpers those cases share.
@Suppress("TooManyFunctions")
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationIdRingTest {

    private val screen = ShownNotificationsFake()

    private val ring = NotificationIdRing(shownNotifications = screen::read)

    private suspend fun nextId(): Int = ring.withNextId { id: Int -> id }

    @Test
    fun startsAtTenWithNothingOnScreenThenMovesToEleven() = runTest {
        //Given
        screen.shown = emptyList()

        //When
        val ids = listOf(nextId(), nextId())

        //Then
        assertEquals(listOf(10, 11), ids)
    }

    @Test
    fun continuesAfterTheNewestIdOnScreenNotAfterTheHighest() = runTest {
        //Given
        screen.shown = listOf(shown(id = 20, at = 1), shown(id = 15, at = 2))

        //When
        val id = nextId()

        //Then
        assertEquals(16, id)
    }

    @Test
    fun aWrappedRingContinuesAfterTheNewestAcrossTheEnd() = runTest {
        //Given
        screen.shown = listOf(
            shown(id = 28, at = 1),
            shown(id = 29, at = 2),
            shown(id = 10, at = 3)
        )

        //When
        val id = nextId()

        //Then
        assertEquals(11, id)
    }

    @Test
    fun wrapsFromTheLastIdBackToTen() = runTest {
        //Given
        screen.shown = listOf(shown(id = 28, at = 1), shown(id = 29, at = 2))

        //When
        val id = nextId()

        //Then
        assertEquals(10, id)
    }

    @Test
    fun skipsIdsThatAreStillOnScreen() = runTest {
        //Given
        screen.shown = listOf(
            shown(id = 12, at = 1),
            shown(id = 13, at = 2),
            shown(id = 11, at = 3)
        )

        //When
        val id = nextId()

        //Then
        assertEquals(14, id)
    }

    @Test
    fun withAllTwentyOnScreenReusesTheOldestThenTheNextOldest() = runTest {
        //Given
        val postedFirst = 17
        val postedSecond = 23
        val others = (10..29) - postedFirst - postedSecond
        screen.shown = listOf(shown(id = postedFirst, at = 1), shown(id = postedSecond, at = 2)) +
            others.map { shown(id = it, at = 3L + it) }

        //When
        val ids = listOf(nextId(), nextId())

        //Then
        assertEquals(listOf(postedFirst, postedSecond), ids)
    }

    @Test
    fun theIdHandedOutLastCountsAsTakenAndNewestWhenTheListLacksIt() = runTest {
        //Given
        screen.shown = listOf(shown(id = 20, at = 5))
        val first = nextId()

        //When
        val second = nextId()

        //Then
        assertEquals(21, first)
        assertEquals(22, second)
    }

    @Test
    fun theIdHandedOutLastIsNeverTheOneReplaced() = runTest {
        //Given
        screen.shown = (10..29).map { shown(id = it, at = 7) }
        val replaced = nextId()

        //When
        screen.shown = (10..29).map { shown(id = it, at = if (it == replaced) 1L else 7L) }
        val next = nextId()

        //Then
        assertEquals(10, replaced)
        assertEquals(11, next)
    }

    @Test
    fun aDismissedNotificationFreesItsId() = runTest {
        //Given
        val handedOut = mutableListOf<Int>()
        repeat(20) {
            screen.shown = handedOut.mapIndexed { index: Int, id: Int ->
                shown(id = id, at = index.toLong())
            }
            handedOut += nextId()
        }
        // Everything but id 10 was swiped away.
        screen.shown = listOf(shown(id = 10, at = 0))

        //When
        val id = nextId()

        //Then
        assertEquals((10..29).toList(), handedOut)
        assertEquals(11, id)
    }

    @Test
    fun twoPostedAtTheSameMomentCountTheHigherIdAsNewer() = runTest {
        //Given
        screen.shown = listOf(shown(id = 17, at = 5), shown(id = 12, at = 5))

        //When
        val id = nextId()

        //Then
        assertEquals(18, id)
    }

    @Test
    fun ignoresIdsOutsideTheRing() = runTest {
        //Given
        screen.shown = listOf(0, 5, 9, 30, 99, Int.MAX_VALUE).map { shown(id = it, at = 9) }

        //When
        val id = nextId()

        //Then
        assertEquals(10, id)
    }

    @Test
    fun theOrderOfTheListDoesNotChangeTheResult() = runTest {
        //Given
        val entries = listOf(shown(id = 25, at = 3), shown(id = 12, at = 9), shown(id = 13, at = 1))
        val reversedScreen = ShownNotificationsFake().apply { shown = entries.reversed() }
        val reversedRing = NotificationIdRing(shownNotifications = reversedScreen::read)
        screen.shown = entries

        //When
        val id = nextId()
        val idFromReversed = reversedRing.withNextId { it }

        //Then
        assertEquals(14, id)
        assertEquals(id, idFromReversed)
    }

    @Test
    fun readsTheScreenBeforeEveryRequest() = runTest {
        //Given
        screen.shown = emptyList()

        //When
        repeat(3) { nextId() }

        //Then
        assertEquals(3, screen.reads)
    }

    @Test
    fun twoRequestsAtOnceGetDifferentIds() = runTest {
        //Given
        screen.shown = emptyList()
        val postGate = CompletableDeferred<Unit>()
        val first = async {
            ring.withNextId { id: Int ->
                postGate.await()
                id
            }
        }
        val second = async { nextId() }
        advanceUntilIdle()
        // The second request waits for the first one's post before it even reads.
        val readsWhileTheFirstPosts = screen.reads

        //When
        postGate.complete(Unit)

        //Then
        assertEquals(1, readsWhileTheFirstPosts)
        assertEquals(10, first.await())
        assertEquals(11, second.await())
    }

    @Test
    fun aFailedReadFailsTheRequestAndTheNextRequestReadsAgain() = runTest {
        //Given
        screen.shown = emptyList()
        screen.nextFailure = IllegalStateException("no notification service")

        //When
        val failure = assertFailsWith<IllegalStateException> { nextId() }
        val id = nextId()

        //Then
        assertEquals("no notification service", failure.message)
        assertEquals(10, id)
        assertEquals(2, screen.reads)
    }

    @Test
    fun aRequestCancelledDuringItsReadReleasesTheRingAndHandsNothingOut() = runTest {
        //Given
        screen.shown = emptyList()
        screen.readGate = CompletableDeferred()
        val cancelled = launch { nextId() }
        advanceUntilIdle()

        //When
        cancelled.cancel()
        screen.readGate = null
        val id = nextId()

        //Then
        assertTrue(cancelled.isCancelled)
        assertEquals(10, id)
    }

    @Test
    fun aFailingPostLeavesNothingHandedOut() = runTest {
        //Given
        screen.shown = emptyList()

        //When
        assertFailsWith<IllegalStateException> {
            ring.withNextId<Unit> { error("the system refused the notification") }
        }
        val id = nextId()

        //Then
        assertEquals(10, id)
    }

    @Test
    fun anIdPostedUnderItsIdentifierReadsBackWithItsMomentInMilliseconds() {
        //Given
        val identifier = ringNotificationIdentifier(17)

        //When
        val shown = shownNotificationOf(identifier, deliveredAtEpochSeconds = 1_700_000_000.123)

        //Then
        assertEquals(ShownNotification(id = 17, postedAtMillis = 1_700_000_000_123), shown)
    }

    @Test
    fun aMomentBetweenTwoMillisecondsRoundsToTheNearest() {
        //Given
        val identifier = ringNotificationIdentifier(17)

        //When
        val shown = shownNotificationOf(identifier, deliveredAtEpochSeconds = 1.0006)

        //Then
        assertEquals(ShownNotification(id = 17, postedAtMillis = 1001), shown)
    }

    @Test
    fun anIdentifierThatIsNotANumberIsNotTheRings() {
        //Given
        val identifiers = listOf("summary", "ANIME_NOTIFICATION_Frieren_12", "")

        //When
        val shown = identifiers.map { shownNotificationOf(it, deliveredAtEpochSeconds = 1.0) }

        //Then
        assertEquals(listOf(null, null, null), shown)
    }

    @Test
    fun anIdOutsideTheRingReadsBackAsItselfForTheRingToIgnore() {
        //Given
        val identifier = ringNotificationIdentifier(99)

        //When
        val shown = shownNotificationOf(identifier, deliveredAtEpochSeconds = 2.0)

        //Then
        assertEquals(ShownNotification(id = 99, postedAtMillis = 2000), shown)
    }

    private fun shown(id: Int, at: Long): ShownNotification =
        ShownNotification(id = id, postedAtMillis = at)

    /** The notifications a test puts on screen, and what reading them does. */
    private class ShownNotificationsFake {
        var shown: List<ShownNotification> = emptyList()
        var reads = 0
            private set

        /** Thrown by the next read only. */
        var nextFailure: Throwable? = null

        /** Holds every read until it completes. */
        var readGate: CompletableDeferred<Unit>? = null

        suspend fun read(): List<ShownNotification> {
            reads++
            readGate?.await()
            nextFailure?.let { failure: Throwable ->
                nextFailure = null
                throw failure
            }
            return shown
        }
    }
}
