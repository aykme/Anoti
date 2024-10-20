package com.alekseivinogradov.anime_list.api.domain.model

import com.alekseivinogradov.celebrity.api.domain.AnimeId

data class AnimeDetails(
    val nextEpisodesInfo: Map<AnimeId, String?> = mapOf()
)
