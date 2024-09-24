package com.alekseivinogradov.database.room.api.data.mapper

import com.alekseivinogradov.database.api.data.model.AnimeDb
import com.alekseivinogradov.database.room.api.data.model.AnimeDbPlatform

internal fun AnimeDb.toPlatform() = AnimeDbPlatform(
    id = this.id,
    name = this.name,
    imageUrl = this.imageUrl,
    episodesAired = this.episodesAired,
    episodesTotal = this.episodesTotal,
    extraEpisodesInfo = this.extraEpisodesInfo,
    score = this.score,
    releaseStatus = this.releaseStatus,
    episodesViewed = this.episodesViewed,
    isNewEpisode = this.isNewEpisode
)

internal fun AnimeDbPlatform.toKmp() = AnimeDb(
    id = this.id,
    name = this.name,
    imageUrl = this.imageUrl,
    episodesAired = this.episodesAired,
    episodesTotal = this.episodesTotal,
    extraEpisodesInfo = this.extraEpisodesInfo,
    score = this.score,
    releaseStatus = this.releaseStatus,
    episodesViewed = this.episodesViewed,
    isNewEpisode = this.isNewEpisode
)
