package com.alekseivinogradov.anoti.animelist.kmp.api.domain.model

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

data class AnimeDetails(
    val nextEpisodesInfo: Map<AnimeId, String?> = mapOf()
)
