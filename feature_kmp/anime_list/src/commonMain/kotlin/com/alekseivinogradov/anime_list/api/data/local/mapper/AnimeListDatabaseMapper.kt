package com.alekseivinogradov.anime_list.api.data.local.mapper

import com.alekseivinogradov.anime_list.api.domain.model.section.EpisodesInfoTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.section.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.section.ReleaseStatusDomain
import com.alekseivinogradov.database.api.data.model.AnimeDb
import com.alekseivinogradov.database.api.data.model.ReleaseStatusDb

internal fun ListItemDomain.toDb() = AnimeDb(
    id = this.id ?: -1,
    name = this.name,
    imageUrl = this.imageUrl,
    episodesAired = this.episodesAired,
    episodesTotal = this.episodesTotal,
    extraEpisodesInfo = this.extraEpisodesInfo,
    score = this.score,
    releaseStatus = mapReleaseStatusDomainToDb(this.releaseStatus),
    episodesViewed = 0,
    isNewEpisode = false
)

internal fun AnimeDb.toDomain() = ListItemDomain(
    id = if (this.id >= 0) {
        this.id
    } else {
        null
    },
    name = this.name,
    imageUrl = this.imageUrl,
    episodesInfoType = EpisodesInfoTypeDomain.AVAILABLE,
    episodesAired = this.episodesAired,
    episodesTotal = this.episodesTotal,
    extraEpisodesInfo = this.extraEpisodesInfo,
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