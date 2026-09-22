package com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.mapper

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import kotlin.test.Test
import kotlin.test.assertEquals

class AnimeFavoritesDatabaseMapperTest {

    private val populatedListItem = ListItemDomain(
        id = 42,
        name = "Populated anime",
        imageUrl = "https://example.org/cover.jpg",
        episodesAired = 5,
        episodesTotal = 12,
        nextEpisodeAt = "2026-01-02",
        airedOn = "2025-10-01",
        releasedOn = "2026-03-01",
        score = 8.5F,
        releaseStatus = ReleaseStatusDomain.ONGOING,
        episodesViewed = 3,
        isNewEpisode = true,
        isExtraInfoEnabled = true
    )

    private val populatedDbItem = AnimeDbDomain(
        id = 42,
        imageUrl = "https://example.org/cover.jpg",
        name = "Populated anime",
        episodesAired = 5,
        episodesTotal = 12,
        nextEpisodeAt = "2026-01-02",
        airedOn = "2025-10-01",
        releasedOn = "2026-03-01",
        score = 8.5F,
        releaseStatus = ReleaseStatusDb.ONGOING,
        episodesViewed = 3,
        isNewEpisode = true,
        isExtraInfoEnabled = true
    )

    private val bareDbItem = AnimeDbDomain(
        id = 7,
        imageUrl = null,
        name = "Bare anime",
        episodesAired = null,
        episodesTotal = null,
        nextEpisodeAt = null,
        airedOn = null,
        releasedOn = null,
        score = null,
        releaseStatus = ReleaseStatusDb.ANNOUNCED,
        episodesViewed = 0,
        isNewEpisode = false,
        isExtraInfoEnabled = false
    )

    private val bareListItem = ListItemDomain(
        id = 7,
        name = "Bare anime",
        imageUrl = null,
        episodesAired = null,
        episodesTotal = null,
        nextEpisodeAt = null,
        airedOn = null,
        releasedOn = null,
        score = null,
        releaseStatus = ReleaseStatusDomain.ANNOUNCED,
        episodesViewed = 0,
        isNewEpisode = false,
        isExtraInfoEnabled = false
    )

    @Test
    fun toDbCopiesEveryFieldIntoTheDatabaseModel() {
        //Given
        val listItem = populatedListItem

        //When
        val dbItem = listItem.toDb()

        //Then
        assertEquals(populatedDbItem, dbItem)
    }

    @Test
    fun toDomainCopiesEveryFieldIntoTheListItem() {
        //Given
        val dbItem = bareDbItem

        //When
        val listItem = dbItem.toDomain()

        //Then
        assertEquals(bareListItem, listItem)
    }

    @Test
    fun toDbMapsEveryReleaseStatus() {
        //Given
        val expected = mapOf(
            ReleaseStatusDomain.UNKNOWN to ReleaseStatusDb.UNKNOWN,
            ReleaseStatusDomain.ONGOING to ReleaseStatusDb.ONGOING,
            ReleaseStatusDomain.ANNOUNCED to ReleaseStatusDb.ANNOUNCED,
            ReleaseStatusDomain.RELEASED to ReleaseStatusDb.RELEASED
        )

        //When
        val actual = ReleaseStatusDomain.entries.associateWith { releaseStatus ->
            populatedListItem.copy(releaseStatus = releaseStatus).toDb().releaseStatus
        }

        //Then
        assertEquals(expected, actual)
    }

    @Test
    fun toDomainMapsEveryReleaseStatus() {
        //Given
        val expected = mapOf(
            ReleaseStatusDb.UNKNOWN to ReleaseStatusDomain.UNKNOWN,
            ReleaseStatusDb.ONGOING to ReleaseStatusDomain.ONGOING,
            ReleaseStatusDb.ANNOUNCED to ReleaseStatusDomain.ANNOUNCED,
            ReleaseStatusDb.RELEASED to ReleaseStatusDomain.RELEASED
        )

        //When
        val actual = ReleaseStatusDb.entries.associateWith { releaseStatus ->
            populatedDbItem.copy(releaseStatus = releaseStatus).toDomain().releaseStatus
        }

        //Then
        assertEquals(expected, actual)
    }

    @Test
    fun aRoundTripThroughTheDatabaseModelReturnsAnEqualItem() {
        //Given
        val listItems = listOf(populatedListItem, bareListItem)

        //When
        val roundTripped = listItems.map { listItem -> listItem.toDb().toDomain() }

        //Then
        assertEquals(listItems, roundTripped)
    }
}
