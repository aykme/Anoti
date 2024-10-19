package com.alekseivinogradov.anime_list.api.domain.model

import com.alekseivinogradov.anime_base.api.domain.AnimeId

data class AnimeDetails(
    val nextEpisodesInfo: Map<AnimeId, String?> = mapOf()
)
