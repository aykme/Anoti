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
        //Given
        val source = domain

        //When
        val mapped = source.toDb()

        //Then
        assertEquals(entity, mapped)
    }

    @Test
    fun entityToDomainMapsEveryField() {
        //Given
        val source = entity

        //When
        val mapped = source.toDomain()

        //Then
        assertEquals(domain, mapped)
    }

    @Test
    fun domainToDbToDomainRoundTripsToTheOriginalValue() {
        //Given
        val source = domain

        //When
        val roundTripped = source.toDb().toDomain()

        //Then
        assertEquals(domain, roundTripped)
    }

    @Test
    fun theExtraInfoModeSurvivesTheRoundTrip() {
        //Given
        val source = domain.copy(isExtraInfoEnabled = true)

        //When
        val roundTripped = source.toDb().toDomain()

        //Then
        assertEquals(source, roundTripped)
    }
}
