package com.alekseivinogradov.anoti.animebase.kmp.api.data.service

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.ReleaseStatusData
import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeListResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.ITEMS_PER_PAGE

const val ANIME_LIST_APPEND_URL = "api/graphql"

/** The Shikimori endpoint for anime lists, by filter or by id. */
interface ShikimoriApiService {

    /**
     * One page of anime from Shikimori GraphQL, [ITEMS_PER_PAGE] at most.
     *
     * @param page page number to fetch, starting at 1.
     * @param releaseStatus filters by release status (see [ReleaseStatusData]), or null for no
     * filter.
     * @param sort sort order (see [SortData]), or null for the server's default.
     * @param search filters by name substring, or null for no filter.
     * @param ids comma-separated anime ids to restrict the answer to, at most [ITEMS_PER_PAGE],
     * or null for no filter. The server answers them in its own order and leaves out an unknown
     * id.
     */
    suspend fun getAnimeList(
        page: Int,
        releaseStatus: String?,
        sort: String?,
        search: String?,
        ids: String?
    ): AnimeListResponse
}
