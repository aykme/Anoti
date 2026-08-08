package com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.mapper

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.model.AnimeDbEntity

internal fun AnimeDbDomain.toDb() = AnimeDbEntity(
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

internal fun AnimeDbEntity.toDomain() = AnimeDbDomain(
    id = this.id,
    imageUrl = this.imageUrl,
    name = this.name,
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
