package com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.mapper

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.InfoTypeUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ListItemUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.NotificationUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ReleaseStatusUi
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val NEXT_EPISODE_AT = "2026-01-02"
private const val AIRED_ON = "2025-10-01"
private const val RELEASED_ON = "2026-03-01"

class StateToUiModelMapperTest {

    private fun listItem(
        id: AnimeId = 42,
        episodesAired: Int? = 5,
        episodesTotal: Int? = 12,
        releaseStatus: ReleaseStatusDomain = ReleaseStatusDomain.ONGOING,
        isExtraInfoEnabled: Boolean = false
    ): ListItemDomain {
        return ListItemDomain(
            id = id,
            name = "Item $id",
            imageUrl = "https://example.org/$id.jpg",
            episodesAired = episodesAired,
            episodesTotal = episodesTotal,
            nextEpisodeAt = NEXT_EPISODE_AT,
            airedOn = AIRED_ON,
            releasedOn = RELEASED_ON,
            score = 8.5F,
            releaseStatus = releaseStatus,
            episodesViewed = 3,
            isNewEpisode = true,
            isExtraInfoEnabled = isExtraInfoEnabled
        )
    }

    private fun stateOf(vararg listItems: ListItemDomain): AnimeFavoritesMainStore.State {
        return AnimeFavoritesMainStore.State(listItems = listItems.toList())
    }

    @Test
    fun aListItemIsMappedFieldForFieldIntoItsUiCounterpart() {
        //Given
        val state = stateOf(listItem())

        //When
        val uiModel = mapStateToUiModel(state)

        //Then
        assertEquals(
            ListItemUi(
                id = 42,
                imageUrl = "https://example.org/42.jpg",
                score = "8.5",
                infoType = InfoTypeUi.MAIN,
                name = "Item 42",
                availableEpisodesInfo = "5 / 12",
                releaseStatus = ReleaseStatusUi.ONGOING,
                notification = NotificationUi.ENABLED,
                extraEpisodesInfo = NEXT_EPISODE_AT,
                episodesViewed = "3",
                isNewEpisode = true
            ),
            uiModel.listItems.single()
        )
    }

    @Test
    fun aNullScoreBecomesAnEmptyString() {
        //Given
        val state = stateOf(listItem().copy(score = null))

        //When
        val uiModel = mapStateToUiModel(state)

        //Then
        assertEquals("", uiModel.listItems.single().score)
    }

    @Test
    fun theExtraInfoFlagSelectsTheItemsInfoType() {
        //Given
        val state = stateOf(
            listItem(id = 1, isExtraInfoEnabled = false),
            listItem(id = 2, isExtraInfoEnabled = true)
        )

        //When
        val uiModel = mapStateToUiModel(state)

        //Then
        assertEquals(
            listOf(InfoTypeUi.MAIN, InfoTypeUi.EXTRA),
            uiModel.listItems.map(ListItemUi::infoType)
        )
    }

    @Test
    fun aNonReleasedAnimeWithoutAnAiredCountShowsZeroAiredEpisodes() {
        //Given
        val state = stateOf(
            listItem(
                releaseStatus = ReleaseStatusDomain.ONGOING,
                episodesAired = null,
                episodesTotal = 12
            )
        )

        //When
        val uiModel = mapStateToUiModel(state)

        //Then
        assertEquals("0 / 12", uiModel.listItems.single().availableEpisodesInfo)
    }

    @Test
    fun aReleasedAnimeCountsItsTotalAsTheAiredEpisodes() {
        //Given
        val state = stateOf(
            listItem(
                releaseStatus = ReleaseStatusDomain.RELEASED,
                episodesAired = 5,
                episodesTotal = 12
            )
        )

        //When
        val uiModel = mapStateToUiModel(state)

        //Then
        assertEquals("12 / 12", uiModel.listItems.single().availableEpisodesInfo)
    }

    @Test
    fun aReleasedAnimeWithoutATotalFallsBackToTheAiredCount() {
        //Given
        val state = stateOf(
            listItem(
                releaseStatus = ReleaseStatusDomain.RELEASED,
                episodesAired = 5,
                episodesTotal = null
            )
        )

        //When
        val uiModel = mapStateToUiModel(state)

        //Then
        assertEquals("5 / ?", uiModel.listItems.single().availableEpisodesInfo)
    }

    @Test
    fun aReleasedAnimeWithNeitherATotalNorAnAiredCountShowsZero() {
        //Given
        val state = stateOf(
            listItem(
                releaseStatus = ReleaseStatusDomain.RELEASED,
                episodesAired = null,
                episodesTotal = null
            )
        )

        //When
        val uiModel = mapStateToUiModel(state)

        //Then
        assertEquals("0 / ?", uiModel.listItems.single().availableEpisodesInfo)
    }

    @Test
    fun aTotalOfZeroRendersAsAQuestionMark() {
        //Given
        val state = stateOf(
            listItem(
                releaseStatus = ReleaseStatusDomain.ONGOING,
                episodesAired = 5,
                episodesTotal = 0
            )
        )

        //When
        val uiModel = mapStateToUiModel(state)

        //Then
        assertEquals("5 / ?", uiModel.listItems.single().availableEpisodesInfo)
    }

    @Test
    fun extraEpisodesInfoFollowsTheReleaseStatus() {
        //Given
        val state = stateOf(
            listItem(id = 1, releaseStatus = ReleaseStatusDomain.ONGOING),
            listItem(id = 2, releaseStatus = ReleaseStatusDomain.ANNOUNCED),
            listItem(id = 3, releaseStatus = ReleaseStatusDomain.RELEASED),
            listItem(id = 4, releaseStatus = ReleaseStatusDomain.UNKNOWN)
        )

        //When
        val uiModel = mapStateToUiModel(state)

        //Then
        assertEquals(
            listOf(NEXT_EPISODE_AT, AIRED_ON, RELEASED_ON, null),
            uiModel.listItems.map(ListItemUi::extraEpisodesInfo)
        )
    }

    @Test
    fun everyReleaseStatusMapsToItsUiCounterpart() {
        //Given
        val expected = mapOf(
            ReleaseStatusDomain.UNKNOWN to ReleaseStatusUi.UNKNOWN,
            ReleaseStatusDomain.ONGOING to ReleaseStatusUi.ONGOING,
            ReleaseStatusDomain.ANNOUNCED to ReleaseStatusUi.ANNOUNCED,
            ReleaseStatusDomain.RELEASED to ReleaseStatusUi.RELEASED
        )

        //When
        val actual = ReleaseStatusDomain.entries.associateWith { releaseStatus ->
            val state = stateOf(listItem(releaseStatus = releaseStatus))
            mapStateToUiModel(state).listItems.single().releaseStatus
        }

        //Then
        assertEquals(expected, actual)
    }

    @Test
    fun everyContentTypeMapsToItsUiCounterpart() {
        //Given
        val expected = listOf(
            ContentTypeDomain.LOADING(hasMinimumDuration = false) to ContentTypeUi.LOADING,
            ContentTypeDomain.LOADING(hasMinimumDuration = true) to ContentTypeUi.LOADING,
            ContentTypeDomain.LOADED to ContentTypeUi.LOADED,
            ContentTypeDomain.EMPTY to ContentTypeUi.EMPTY
        )

        //When
        val actual = expected.map { (contentType, _) ->
            val state = AnimeFavoritesMainStore.State(contentType = contentType)
            contentType to mapStateToUiModel(state).contentType
        }

        //Then
        assertEquals(expected, actual)
    }

    @Test
    fun aStateWithoutItemsProducesAnEmptyUiList() {
        //Given
        val state = AnimeFavoritesMainStore.State()

        //When
        val uiModel = mapStateToUiModel(state)

        //Then
        assertTrue(uiModel.listItems.isEmpty())
    }
}
