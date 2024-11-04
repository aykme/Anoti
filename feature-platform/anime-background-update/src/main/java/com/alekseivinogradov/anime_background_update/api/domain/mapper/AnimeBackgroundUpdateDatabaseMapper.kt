package com.alekseivinogradov.anime_background_update.api.domain.mapper

import com.alekseivinogradov.anime_background_update.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.database.api.domain.model.ReleaseStatusDb

internal fun mapReleaseStatusDomainToDb(releaseStatus: ReleaseStatusDomain): ReleaseStatusDb {
    return when (releaseStatus) {
        ReleaseStatusDomain.ONGOING -> ReleaseStatusDb.ONGOING
        ReleaseStatusDomain.ANNOUNCED -> ReleaseStatusDb.ANNOUNCED
        ReleaseStatusDomain.RELEASED -> ReleaseStatusDb.RELEASED
        ReleaseStatusDomain.UNKNOWN -> ReleaseStatusDb.UNKNOWN
    }
}
