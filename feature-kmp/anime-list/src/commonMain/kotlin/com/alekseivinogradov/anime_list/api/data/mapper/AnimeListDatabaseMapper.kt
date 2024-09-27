package com.alekseivinogradov.anime_list.api.data.mapper

import com.alekseivinogradov.anime_list.api.domain.model.EpisodesInfoTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.database.api.data.model.AnimeDb
import com.alekseivinogradov.database.api.data.model.ReleaseStatusDb

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
    episodesViewed = 0,
    isNewEpisode = false
)

internal fun AnimeDb.toDomain() = ListItemDomain(
    id = this.id,
    name = this.name,
    imageUrl = this.imageUrl,
    episodesInfoType = EpisodesInfoTypeDomain.AVAILABLE,
    episodesAired = this.episodesAired,
    episodesTotal = this.episodesTotal,
    nextEpisodeAt = this.nextEpisodeAt,
    airedOn = this.airedOn,
    releasedOn = this.releasedOn,
    score = this.score,
    releaseStatus = mapReleaseStatusDbToDomain(this.releaseStatus)
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
