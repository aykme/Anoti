package com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlin.test.Test
import kotlin.test.assertEquals

private const val FIRST_ID = 11
private const val SECOND_ID = 22

class DatabaseStoreStateToMainStoreIntentMapperTest {

    private fun dbItem(id: AnimeId, name: String = "Frieren") = AnimeDbDomain(
        id = id,
        imageUrl = null,
        name = name,
        episodesAired = 7,
        episodesTotal = 28,
        nextEpisodeAt = null,
        airedOn = null,
        releasedOn = null,
        score = 9.1F,
        releaseStatus = ReleaseStatusDb.ONGOING,
        episodesViewed = 0,
        isNewEpisode = false
    )

    @Test
    fun everySavedItemIdBecomesAnEnabledNotificationId() {
        //Given
        val state = AnimeDatabaseStore.State(
            animeDatabaseItems = listOf(dbItem(FIRST_ID), dbItem(SECOND_ID))
        )

        //When
        val intent = mapDatabaseStoreStateToMainStoreIntent(state)

        //Then
        assertEquals(
            AnimeListMainStore.Intent.UpdateEnabledNotificationIds(setOf(FIRST_ID, SECOND_ID)),
            intent
        )
    }

    @Test
    fun anEmptyDatabaseLeavesNoNotificationEnabled() {
        //Given
        val state = AnimeDatabaseStore.State()

        //When
        val intent = mapDatabaseStoreStateToMainStoreIntent(state)

        //Then
        assertEquals(AnimeListMainStore.Intent.UpdateEnabledNotificationIds(emptySet()), intent)
    }

    @Test
    fun itemsSharingAnIdCollapseIntoASingleEnabledNotificationId() {
        //Given
        val state = AnimeDatabaseStore.State(
            animeDatabaseItems = listOf(
                dbItem(FIRST_ID, name = "Frieren"),
                dbItem(FIRST_ID, name = "Frieren rerun")
            )
        )

        //When
        val intent = mapDatabaseStoreStateToMainStoreIntent(state)

        //Then
        assertEquals(
            AnimeListMainStore.Intent.UpdateEnabledNotificationIds(setOf(FIRST_ID)),
            intent
        )
    }
}
