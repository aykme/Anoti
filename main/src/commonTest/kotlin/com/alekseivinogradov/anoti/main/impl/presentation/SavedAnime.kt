package com.alekseivinogradov.anoti.main.impl.presentation

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb

/** A saved anime with only the fields the bar's badge counts on. */
internal fun savedAnime(id: Int, hasNewEpisode: Boolean) = AnimeDbDomain(
    id = id,
    imageUrl = null,
    name = "Anime $id",
    episodesAired = null,
    episodesTotal = null,
    nextEpisodeAt = null,
    airedOn = null,
    releasedOn = null,
    score = null,
    releaseStatus = ReleaseStatusDb.ONGOING,
    episodesViewed = 0,
    isNewEpisode = hasNewEpisode
)
