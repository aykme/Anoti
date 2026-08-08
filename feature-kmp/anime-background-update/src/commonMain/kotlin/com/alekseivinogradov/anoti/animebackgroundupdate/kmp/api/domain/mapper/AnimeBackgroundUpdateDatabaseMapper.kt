package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.mapper

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb

fun mapReleaseStatusDomainToDb(releaseStatus: ReleaseStatusDomain): ReleaseStatusDb {
    return when (releaseStatus) {
        ReleaseStatusDomain.ONGOING -> ReleaseStatusDb.ONGOING
        ReleaseStatusDomain.ANNOUNCED -> ReleaseStatusDb.ANNOUNCED
        ReleaseStatusDomain.RELEASED -> ReleaseStatusDb.RELEASED
        ReleaseStatusDomain.UNKNOWN -> ReleaseStatusDb.UNKNOWN
    }
}
