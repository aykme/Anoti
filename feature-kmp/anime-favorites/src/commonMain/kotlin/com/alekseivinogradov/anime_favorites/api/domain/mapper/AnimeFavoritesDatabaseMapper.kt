package com.alekseivinogradov.anime_favorites.api.domain.mapper

import com.alekseivinogradov.anime_favorites.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_favorites.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.database.api.domain.model.AnimeDb
import com.alekseivinogradov.database.api.domain.model.ReleaseStatusDb

internal fun ListItemDomain.toDb() = AnimeDb(
    id = this.id,
    name = this.name,
    imageUrl = this.imageUrl,
    episodesAired = this.episodesAired,
    episodesTotal = this.episodesTotal,
    nextEpisodeAt = this.nextEpisodeAt,
    airedOn = this.airedOn,
    releasedOn = this.releasedOn,
    score = this.score,
    releaseStatus = mapReleaseStatusDomainToDb(this.releaseStatus),
    episodesViewed = this.episodesViewed,
    isNewEpisode = this.isNewEpisode
)

internal fun AnimeDb.toDomain() = ListItemDomain(
    id = this.id,
    name = this.name,
    imageUrl = this.imageUrl,
    episodesAired = this.episodesAired,
    episodesTotal = this.episodesTotal,
    nextEpisodeAt = this.nextEpisodeAt,
    airedOn = this.airedOn,
    releasedOn = this.releasedOn,
    score = this.score,
    releaseStatus = mapReleaseStatusDbToDomain(this.releaseStatus),
    episodesViewed = this.episodesViewed,
    isNewEpisode = this.isNewEpisode
)

private fun mapReleaseStatusDomainToDb(releaseStatus: ReleaseStatusDomain): ReleaseStatusDb {
    return when (releaseStatus) {
        ReleaseStatusDomain.UNKNOWN -> ReleaseStatusDb.UNKNOWN
        ReleaseStatusDomain.ONGOING -> ReleaseStatusDb.ONGOING
        ReleaseStatusDomain.ANNOUNCED -> ReleaseStatusDb.ANNOUNCED
        ReleaseStatusDomain.RELEASED -> ReleaseStatusDb.RELEASED
    }
}

private fun mapReleaseStatusDbToDomain(releaseStatus: ReleaseStatusDb): ReleaseStatusDomain {
    return when (releaseStatus) {
        ReleaseStatusDb.UNKNOWN -> ReleaseStatusDomain.UNKNOWN
        ReleaseStatusDb.ONGOING -> ReleaseStatusDomain.ONGOING
        ReleaseStatusDb.ANNOUNCED -> ReleaseStatusDomain.ANNOUNCED
        ReleaseStatusDb.RELEASED -> ReleaseStatusDomain.RELEASED
    }
}
