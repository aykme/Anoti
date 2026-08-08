package com.alekseivinogradov.anoti.animelist.kmp.api.domain.model

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

/**
 * Extra per-item details fetched on demand for a section's list.
 *
 * @param nextEpisodesInfo next-episode air date/time by anime id, or null per id if none is
 * scheduled.
 */
data class AnimeDetails(
    val nextEpisodesInfo: Map<AnimeId, String?> = mapOf()
)
