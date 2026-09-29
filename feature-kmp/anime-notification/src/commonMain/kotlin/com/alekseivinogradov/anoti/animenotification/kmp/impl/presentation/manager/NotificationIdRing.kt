package com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.manager

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.roundToLong

/**
 * A notification the platform shows for this app.
 *
 * @param id the id it was posted under.
 * @param postedAtMillis when it was last posted, in epoch milliseconds.
 */
internal data class ShownNotification(val id: Int, val postedAtMillis: Long)

/** The text identifier a platform that names its notifications by text posts [id] under. */
internal fun ringNotificationIdentifier(id: Int): String = id.toString()

/**
 * Reads a notification shown under [identifier] back, or `null` when the identifier is not a
 * number and so was not written by [ringNotificationIdentifier].
 *
 * @param deliveredAtEpochSeconds when it was shown, in epoch seconds with a fraction.
 */
internal fun shownNotificationOf(
    identifier: String,
    deliveredAtEpochSeconds: Double
): ShownNotification? = identifier.toIntOrNull()?.let { id: Int ->
    ShownNotification(
        id = id,
        postedAtMillis = (deliveredAtEpochSeconds * MILLIS_PER_SECOND).roundToLong()
    )
}

private const val MILLIS_PER_SECOND = 1000

/**
 * Hands out the ids of new-episode notifications, from 10 to 29. The next id is the first free
 * one after the newest notification on screen. With all twenty on screen, the oldest is replaced.
 *
 * The platform's own list is the only record. So a new process continues after what an earlier
 * one left on screen, and a dismissed notification frees its id. The id handed out last is the
 * exception: it counts as taken, since the platform may not list it yet.
 *
 * @param shownNotifications reads the notifications on screen. Their order means nothing.
 */
internal class NotificationIdRing(
    private val shownNotifications: suspend () -> List<ShownNotification>
) {

    private val mutex = Mutex()

    // A platform can apply a post after it returns, so a read right after it may still miss it.
    private var lastHandedOutId: Int? = null

    /**
     * Picks the next id and runs [post] with it while no other request can pick one. The id counts
     * as handed out only once [post] returns.
     */
    suspend fun <T> withNextId(post: suspend (id: Int) -> T): T = mutex.withLock {
        val id = nextId(shownNotifications())
        val result = post(id)
        lastHandedOutId = id
        result
    }

    private fun nextId(shown: List<ShownNotification>): Int {
        val oldestFirst = shown
            .filter { it.id in FIRST_ID..LAST_ID }
            .sortedWith(compareBy({ it.postedAtMillis }, { it.id }))
            .map { it.id }
            .let { ids: List<Int> ->
                val lastId = lastHandedOutId ?: return@let ids
                ids - lastId + lastId
            }
        val newest = oldestFirst.lastOrNull() ?: return FIRST_ID
        return (1 until RING_SIZE)
            .map { step: Int -> FIRST_ID + (newest - FIRST_ID + step) % RING_SIZE }
            .firstOrNull { it !in oldestFirst }
            ?: oldestFirst.first()
    }

    private companion object {
        const val FIRST_ID = 10
        const val LAST_ID = 29
        const val RING_SIZE = LAST_ID - FIRST_ID + 1
    }
}
