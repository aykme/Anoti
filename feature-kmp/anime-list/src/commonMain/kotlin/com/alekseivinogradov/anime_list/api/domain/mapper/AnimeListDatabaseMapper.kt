package com.alekseivinogradov.anime_list.api.domain.mapper

import com.alekseivinogradov.anime_base.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anime_database.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anime_database.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain

internal fun ListItemDomain.toDb() = AnimeDbDomain(
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

private fun mapReleaseStatusDomainToDb(releaseStatus: ReleaseStatusDomain): ReleaseStatusDb {
    return when (releaseStatus) {
        ReleaseStatusDomain.UNKNOWN -> ReleaseStatusDb.UNKNOWN
        ReleaseStatusDomain.ONGOING -> ReleaseStatusDb.ONGOING
        ReleaseStatusDomain.ANNOUNCED -> ReleaseStatusDb.ANNOUNCED
        ReleaseStatusDomain.RELEASED -> ReleaseStatusDb.RELEASED
    }
}
