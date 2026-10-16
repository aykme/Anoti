package com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper

import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeListResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeResponse

/**
 * The anime of the answer. Data that comes together with errors is used.
 *
 * @throws IllegalStateException when the answer carries no data, which GraphQL sends for a query
 * that failed as a whole. The message names only the count of errors: their text can echo the
 * search the user typed.
 */
fun AnimeListResponse.toAnimeResponses(): List<AnimeResponse> =
    data?.animes ?: error("GraphQL answered without data, with ${errors.orEmpty().size} errors")
