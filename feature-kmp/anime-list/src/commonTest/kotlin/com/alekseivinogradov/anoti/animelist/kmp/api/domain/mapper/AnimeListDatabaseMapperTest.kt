package com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import kotlin.test.Test
import kotlin.test.assertEquals

class AnimeListDatabaseMapperTest {

    private fun listItem(
        releaseStatus: ReleaseStatusDomain = ReleaseStatusDomain.ONGOING
    ) = ListItemDomain(
        id = 42,
        name = "Frieren",
        imageUrl = "https://example.com/frieren.jpg",
        episodesAired = 7,
        episodesTotal = 28,
        nextEpisodeAt = "2026-01-05T18:00:00.000+03:00",
        airedOn = "2023-09-29",
        releasedOn = "2024-03-22",
        score = 9.1F,
        releaseStatus = releaseStatus
    )

    @Test
    fun aFreshlySavedItemKeepsItsFieldsAndStartsUnwatched() {
        //Given
        val listItem = listItem()

        //When
        val dbItem = listItem.toDb()

        //Then
        assertEquals(
            AnimeDbDomain(
                id = 42,
                imageUrl = "https://example.com/frieren.jpg",
                name = "Frieren",
                episodesAired = 7,
                episodesTotal = 28,
                nextEpisodeAt = "2026-01-05T18:00:00.000+03:00",
                airedOn = "2023-09-29",
                releasedOn = "2024-03-22",
                score = 9.1F,
                releaseStatus = ReleaseStatusDb.ONGOING,
                episodesViewed = 0,
                isNewEpisode = false
            ),
            dbItem
        )
    }

    @Test
    fun eachReleaseStatusMapsToItsDatabaseCounterpart() {
        //Given
        val listItemsByStatus = ReleaseStatusDomain.entries.associateWith { listItem(it) }

        //When
        val dbStatuses = listItemsByStatus.mapValues { (_, listItem) ->
            listItem.toDb().releaseStatus
        }

        //Then
        assertEquals(
            mapOf(
                ReleaseStatusDomain.UNKNOWN to ReleaseStatusDb.UNKNOWN,
                ReleaseStatusDomain.ONGOING to ReleaseStatusDb.ONGOING,
                ReleaseStatusDomain.ANNOUNCED to ReleaseStatusDb.ANNOUNCED,
                ReleaseStatusDomain.RELEASED to ReleaseStatusDb.RELEASED
            ),
            dbStatuses
        )
    }
}
