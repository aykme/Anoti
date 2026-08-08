package com.alekseivinogradov.anoti.animebase.kmp.api.data.service

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.ReleaseStatusData
import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeDetailsResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeShortResponse
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

const val ANIME_LIST_APPEND_URL = "api/animes"

/** Shikimori endpoints for the anime list and a single anime's details. */
interface ShikimoriApiService {

    /**
     * @param page page number to fetch, starting at 1.
     * @param releaseStatus filters by release status (see [ReleaseStatusData]), or null for no
     * filter.
     * @param sort sort order (see [SortData]), or null for the API default.
     * @param search filters by name substring, or null for no filter.
     * @param ids comma-separated anime ids to restrict the result to, or null for no filter.
     */
    suspend fun getAnimeList(
        page: Int,
        releaseStatus: String?,
        sort: String?,
        search: String?,
        ids: String?
    ): List<AnimeShortResponse>

    /** Fetches full details for the anime with the given [id]. */
    suspend fun getAnimeById(id: AnimeId): AnimeDetailsResponse
}
