package com.alekseivinogradov.anoti.animebase.kmp.api.domain.source

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

/**
 * Looks up real posters by anime id. REST hands out a placeholder for every poster uploaded
 * since 2024-06; only GraphQL has them.
 *
 * Best effort: it never fails. A failed lookup gives an empty map, and an id missing from the
 * map means "keep the REST image".
 */
interface AnimePosterSource {

    /** @param ids at most one page, [ITEMS_PER_PAGE]; the server caps one answer at 50. */
    suspend fun getPosterUrls(ids: List<AnimeId>): Map<AnimeId, String>
}
