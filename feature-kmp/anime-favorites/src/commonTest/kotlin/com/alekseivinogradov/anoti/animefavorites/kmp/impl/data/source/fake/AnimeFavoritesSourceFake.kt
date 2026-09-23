package com.alekseivinogradov.anoti.animefavorites.kmp.impl.data.source.fake

import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import kotlin.coroutines.cancellation.CancellationException

/**
 * Answers a details fetch from [answer] and records what was asked. Hand it a lambda that
 * suspends, throws or fails to cover the case under test.
 *
 * The default throws, but a caller can turn that into an error path of its own. Assert on
 * [callCount] to pin that a call was never made.
 *
 * @param answer what to answer with, given the id asked for and which call this is, counting
 * from one.
 */
class AnimeFavoritesSourceFake(
    private val answer: suspend (id: AnimeId, callNumber: Int) -> CallResult<ListItemDomain> =
        { _, _ -> error("AnimeFavoritesSourceFake was called with no answer configured") }
) : AnimeFavoritesSource {

    /** How many times an item was asked for. */
    var callCount = 0
        private set

    /** Whether an answer was ever asked for at all. */
    val wasCalled: Boolean get() = callCount > 0

    /** Which calls were canceled before they answered, counting from one. */
    val canceledCalls: List<Int>
        field = mutableListOf<Int>()

    /** Whether any call was canceled before it answered. */
    val wasCanceled: Boolean get() = canceledCalls.isNotEmpty()

    override suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain> {
        callCount++
        val callNumber = callCount
        try {
            return answer(id, callNumber)
        } catch (e: CancellationException) {
            canceledCalls += callNumber
            throw e
        }
    }
}
