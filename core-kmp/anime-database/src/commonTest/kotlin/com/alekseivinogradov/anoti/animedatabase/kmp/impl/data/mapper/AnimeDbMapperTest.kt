package com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.mapper

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.model.AnimeDbEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class AnimeDbMapperTest {

    private val domain = AnimeDbDomain(
        id = 7,
        imageUrl = "https://example.com/image.png",
        name = "Attack on Titan",
        episodesAired = 12,
        episodesTotal = 25,
        nextEpisodeAt = "2026-08-16T12:00:00Z",
        airedOn = "2013-04-07",
        releasedOn = null,
        score = 8.5f,
        releaseStatus = ReleaseStatusDb.ONGOING,
        episodesViewed = 11,
        isNewEpisode = true
    )

    private val entity = AnimeDbEntity(
        id = 7,
        name = "Attack on Titan",
        imageUrl = "https://example.com/image.png",
        episodesAired = 12,
        episodesTotal = 25,
        nextEpisodeAt = "2026-08-16T12:00:00Z",
        airedOn = "2013-04-07",
        releasedOn = null,
        score = 8.5f,
        releaseStatus = ReleaseStatusDb.ONGOING,
        episodesViewed = 11,
        isNewEpisode = true
    )

    @Test
    fun domainToDbMapsEveryField() {
        assertEquals(entity, domain.toDb())
    }

    @Test
    fun entityToDomainMapsEveryField() {
        assertEquals(domain, entity.toDomain())
    }

    @Test
    fun domainToDbToDomainRoundTripsToTheOriginalValue() {
        assertEquals(domain, domain.toDb().toDomain())
    }
}
