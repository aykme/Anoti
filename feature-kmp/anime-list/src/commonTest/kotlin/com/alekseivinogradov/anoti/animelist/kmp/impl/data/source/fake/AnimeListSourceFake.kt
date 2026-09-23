package com.alekseivinogradov.anoti.animelist.kmp.impl.data.source.fake

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

/** One request for a page of a section. */
data class PageCall(val page: Int, val sort: SortData)

/** One request for a page of search results. */
data class SearchCall(val page: Int, val search: String, val sort: SortData)

/**
 * Answers each of the four listings from its own lambda and records what was asked for. A
 * listing left unconfigured throws when called.
 *
 * A caller that wraps the call turns that throw into an error path of its own. A paginator does,
 * and so does a scope whose handler swallows. Assert on the recorded calls to pin that a listing
 * was never reached.
 *
 * @param ongoing answers the on-air listing.
 * @param announced answers the upcoming listing.
 * @param search answers the search listing.
 * @param byId answers a single anime's details.
 */
class AnimeListSourceFake(
    private val ongoing: suspend (page: Int, sort: SortData) -> CallResult<List<ListItemDomain>> =
        { _, _ -> notConfigured("getOngoingList") },
    private val announced: suspend (page: Int, sort: SortData) -> CallResult<List<ListItemDomain>> =
        { _, _ -> notConfigured("getAnnouncedList") },
    private val search: suspend (
        page: Int,
        search: String,
        sort: SortData
    ) -> CallResult<List<ListItemDomain>> = { _, _, _ -> notConfigured("getListBySearch") },
    private val byId: suspend (id: AnimeId) -> CallResult<ListItemDomain> =
        { notConfigured("getItemById") }
) : AnimeListSource {

    /** Every on-air page asked for, oldest first. */
    val ongoingCalls: List<PageCall>
        field = mutableListOf<PageCall>()

    /** Every upcoming page asked for, oldest first. */
    val announcedCalls: List<PageCall>
        field = mutableListOf<PageCall>()

    /** Every search page asked for, oldest first. */
    val searchCalls: List<SearchCall>
        field = mutableListOf<SearchCall>()

    /** Every anime id asked for, oldest first. */
    val requestedIds: List<AnimeId>
        field = mutableListOf<AnimeId>()

    override suspend fun getOngoingList(
        page: Int,
        sort: SortData
    ): CallResult<List<ListItemDomain>> {
        ongoingCalls += PageCall(page = page, sort = sort)
        return ongoing(page, sort)
    }

    override suspend fun getAnnouncedList(
        page: Int,
        sort: SortData
    ): CallResult<List<ListItemDomain>> {
        announcedCalls += PageCall(page = page, sort = sort)
        return announced(page, sort)
    }

    override suspend fun getListBySearch(
        page: Int,
        search: String,
        sort: SortData
    ): CallResult<List<ListItemDomain>> {
        searchCalls += SearchCall(page = page, search = search, sort = sort)
        return this.search(page, search, sort)
    }

    override suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain> {
        requestedIds += id
        return byId(id)
    }
}

private fun notConfigured(method: String): Nothing =
    error("AnimeListSourceFake.$method was called with no answer configured")
