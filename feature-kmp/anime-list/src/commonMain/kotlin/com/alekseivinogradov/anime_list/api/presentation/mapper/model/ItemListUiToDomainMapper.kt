package com.alekseivinogradov.anime_list.api.presentation.mapper.model

import com.alekseivinogradov.anime_base.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.presentation.model.ListItemUi
import com.alekseivinogradov.anime_list.api.presentation.model.item_content.ReleaseStatusUi

fun ListItemUi.toDomain(): ListItemDomain {
    return ListItemDomain(
        id = this.id,
        name = this.name,
        imageUrl = this.imageUrl,
        episodesAired = this.episodesAired,
        episodesTotal = this.episodesTotal,
        nextEpisodeAt = this.nextEpisodeAt,
        airedOn = this.airedOn,
        releasedOn = this.releasedOn,
        score = this.score.toFloatOrNull(),
        releaseStatus = mapReleaseStatusUiToDomain(this.releaseStatus)
    )
}

private fun mapReleaseStatusUiToDomain(releaseStatus: ReleaseStatusUi): ReleaseStatusDomain {
    return when (releaseStatus) {
        ReleaseStatusUi.ONGOING -> ReleaseStatusDomain.ONGOING
        ReleaseStatusUi.ANNOUNCED -> ReleaseStatusDomain.ANNOUNCED
        ReleaseStatusUi.RELEASED -> ReleaseStatusDomain.RELEASED
        ReleaseStatusUi.UNKNOWN -> ReleaseStatusDomain.UNKNOWN
    }
}
