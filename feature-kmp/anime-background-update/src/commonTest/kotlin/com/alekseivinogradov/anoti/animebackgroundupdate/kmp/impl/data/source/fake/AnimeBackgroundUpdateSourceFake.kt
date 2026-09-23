package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.data.source.fake

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.source.AnimeBackgroundUpdateSource
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

/**
 * Answers every fetch from [answer] and records the ids it was asked for.
 *
 * Left to itself it answers with an empty success rather than throwing. The real source keeps
 * its whole body behind `SafeApi`, which turns a throwable into a result, so a throw from here
 * stands for nothing the real one can do. Cancellation is the exception: `SafeApi` rethrows it.
 * Assert on [requestedIds] to pin what was asked for.
 *
 * @param answer decides the outcome of one fetch from the ids it was given.
 */
class AnimeBackgroundUpdateSourceFake(
    private val answer: suspend (ids: String) -> CallResult<List<ListItemDomain>> =
        { CallResult.Success(listOf()) }
) : AnimeBackgroundUpdateSource {

    /** The id string of every fetch, oldest first. */
    val requestedIds: List<String>
        field = mutableListOf<String>()

    override suspend fun getListByIds(ids: String): CallResult<List<ListItemDomain>> {
        requestedIds += ids
        return answer(ids)
    }
}
