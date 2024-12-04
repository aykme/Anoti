package com.alekseivinogradov.anime_database.room.api.data.mapper

import com.alekseivinogradov.anime_database.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anime_database.room.api.data.model.AnimeDbPlatform

internal fun AnimeDbDomain.toPlatform() = AnimeDbPlatform(
    id = this.id,
    name = this.name,
    imageUrl = this.imageUrl,
    episodesAired = this.episodesAired,
    episodesTotal = this.episodesTotal,
    nextEpisodeAt = this.nextEpisodeAt,
    airedOn = this.airedOn,
    releasedOn = this.releasedOn,
    score = this.score,
    releaseStatus = this.releaseStatus,
    episodesViewed = this.episodesViewed,
    isNewEpisode = this.isNewEpisode
)

internal fun AnimeDbPlatform.toKmp() = AnimeDbDomain(
    id = this.id,
    name = this.name,
    imageUrl = this.imageUrl,
    episodesAired = this.episodesAired,
    episodesTotal = this.episodesTotal,
    nextEpisodeAt = this.nextEpisodeAt,
    airedOn = this.airedOn,
    releasedOn = this.releasedOn,
    score = this.score,
    releaseStatus = this.releaseStatus,
    episodesViewed = this.episodesViewed,
    isNewEpisode = this.isNewEpisode
)
