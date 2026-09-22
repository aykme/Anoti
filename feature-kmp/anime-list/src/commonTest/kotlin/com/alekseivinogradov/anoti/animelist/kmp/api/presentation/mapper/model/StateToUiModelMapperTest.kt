package com.alekseivinogradov.anoti.animelist.kmp.api.presentation.mapper.model

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.AnimeDetails
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SearchDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionContentDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionHatDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.ListItemUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.SearchUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.SectionHatUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.EpisodesInfoTypeUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.NotificationUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.ReleaseStatusUi
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val FIRST_ID = 11
private const val SECOND_ID = 22
private const val THIRD_ID = 33

private const val ITEM_NEXT_EPISODE_AT = "2023-10-06T18:00:00.000+03:00"
private const val DETAILS_NEXT_EPISODE_AT = "2026-01-05T18:00:00.000+03:00"

class StateToUiModelMapperTest {

    private fun listItem(
        id: AnimeId,
        score: Float? = 9.1F,
        releaseStatus: ReleaseStatusDomain = ReleaseStatusDomain.ONGOING
    ) = ListItemDomain(
        id = id,
        name = "Frieren $id",
        imageUrl = "https://example.com/$id.jpg",
        episodesAired = 7,
        episodesTotal = 28,
        nextEpisodeAt = ITEM_NEXT_EPISODE_AT,
        airedOn = "2023-09-29",
        releasedOn = "2024-03-22",
        score = score,
        releaseStatus = releaseStatus
    )

    private fun renderedItems(
        content: SectionContentDomain,
        enabledNotificationIds: Set<AnimeId> = setOf()
    ): List<ListItemUi> {
        val state = AnimeListMainStore.State(
            ongoingContent = content,
            enabledNotificationIds = enabledNotificationIds
        )
        return mapStateToUiModel(state).listContent.listItems
    }

    @Test
    fun eachSelectedSectionMapsToItsUiSectionHat() {
        //Given
        val state = AnimeListMainStore.State()

        //When
        val sectionHats = SectionHatDomain.entries.associateWith { section ->
            mapStateToUiModel(state.copy(selectedSection = section)).selectedSection
        }

        //Then
        assertEquals(
            mapOf(
                SectionHatDomain.ONGOINGS to SectionHatUi.ONGOINGS,
                SectionHatDomain.ANNOUNCED to SectionHatUi.ANNOUNCED,
                SectionHatDomain.SEARCH to SectionHatUi.SEARCH
            ),
            sectionHats
        )
    }

    @Test
    fun eachSelectedSectionRendersItsOwnListItems() {
        //Given
        val state = AnimeListMainStore.State(
            ongoingContent = SectionContentDomain(listItems = listOf(listItem(FIRST_ID))),
            announcedContent = SectionContentDomain(listItems = listOf(listItem(SECOND_ID))),
            searchContent = SectionContentDomain(listItems = listOf(listItem(THIRD_ID)))
        )

        //When
        val renderedIds = SectionHatDomain.entries.associateWith { section ->
            val model = mapStateToUiModel(state.copy(selectedSection = section))
            model.listContent.listItems.map { it.id }
        }

        //Then
        assertEquals(
            mapOf(
                SectionHatDomain.ONGOINGS to listOf(FIRST_ID),
                SectionHatDomain.ANNOUNCED to listOf(SECOND_ID),
                SectionHatDomain.SEARCH to listOf(THIRD_ID)
            ),
            renderedIds
        )
    }

    @Test
    fun theContentTypeComesFromTheSelectedSection() {
        //Given
        val state = AnimeListMainStore.State(
            ongoingContent = SectionContentDomain(contentType = ContentTypeDomain.LOADED),
            announcedContent = SectionContentDomain(contentType = ContentTypeDomain.LOADING),
            searchContent = SectionContentDomain(contentType = ContentTypeDomain.ERROR)
        )

        //When
        val contentTypes = SectionHatDomain.entries.associateWith { section ->
            mapStateToUiModel(state.copy(selectedSection = section)).contentType
        }

        //Then
        assertEquals(
            mapOf(
                SectionHatDomain.ONGOINGS to ContentTypeUi.LOADED,
                SectionHatDomain.ANNOUNCED to ContentTypeUi.LOADING,
                SectionHatDomain.SEARCH to ContentTypeUi.ERROR
            ),
            contentTypes
        )
    }

    @Test
    fun theSearchBarVisibilityMapsToItsUiCounterpart() {
        //Given
        val state = AnimeListMainStore.State()

        //When
        val searchUis = SearchDomain.Type.entries.associateWith { type ->
            mapStateToUiModel(state.copy(search = SearchDomain(type = type))).search
        }

        //Then
        assertEquals(
            mapOf(
                SearchDomain.Type.HIDDEN to SearchUi.HIDDEN,
                SearchDomain.Type.SHOWN to SearchUi.SHOWN
            ),
            searchUis
        )
    }

    @Test
    fun theResetListPositionFlagIsCarriedIntoTheListContent() {
        //Given
        val state = AnimeListMainStore.State(isNeedToResetListPositon = true)

        //When
        val model = mapStateToUiModel(state)

        //Then
        assertTrue(model.listContent.isNeedToResetListPositon)
    }

    @Test
    fun aListItemCarriesItsDescriptiveFieldsIntoTheUiItem() {
        //Given
        val content = SectionContentDomain(listItems = listOf(listItem(FIRST_ID)))

        //When
        val item = renderedItems(content).single()

        //Then
        assertEquals(
            ListItemUi(
                id = FIRST_ID,
                name = "Frieren $FIRST_ID",
                imageUrl = "https://example.com/$FIRST_ID.jpg",
                episodesInfoType = EpisodesInfoTypeUi.AVAILABLE,
                episodesAired = 7,
                episodesTotal = 28,
                nextEpisodeAt = null,
                airedOn = "2023-09-29",
                releasedOn = "2024-03-22",
                score = "9.1",
                releaseStatus = ReleaseStatusUi.ONGOING,
                notification = NotificationUi.DISABLED
            ),
            item
        )
    }

    @Test
    fun onlyItemsWithExtraEpisodesInfoEnabledRenderTheExtraVariant() {
        //Given
        val content = SectionContentDomain(
            listItems = listOf(listItem(FIRST_ID), listItem(SECOND_ID)),
            enabledExtraEpisodesInfoIds = setOf(FIRST_ID)
        )

        //When
        val items = renderedItems(content)

        //Then
        assertEquals(
            listOf(EpisodesInfoTypeUi.EXTRA, EpisodesInfoTypeUi.AVAILABLE),
            items.map { it.episodesInfoType }
        )
    }

    @Test
    fun theNextEpisodeDateComesFromTheFetchedDetailsAndIsNullWithoutThem() {
        //Given
        val content = SectionContentDomain(
            listItems = listOf(listItem(FIRST_ID), listItem(SECOND_ID)),
            animeDetails = AnimeDetails(
                nextEpisodesInfo = mapOf(FIRST_ID to DETAILS_NEXT_EPISODE_AT)
            )
        )

        //When
        val items = renderedItems(content)

        //Then
        assertEquals(
            listOf(DETAILS_NEXT_EPISODE_AT, null),
            items.map { it.nextEpisodeAt }
        )
    }

    @Test
    fun anItemWithoutAScoreRendersAnEmptyScoreString() {
        //Given
        val content = SectionContentDomain(
            listItems = listOf(listItem(FIRST_ID, score = null))
        )

        //When
        val item = renderedItems(content).single()

        //Then
        assertEquals("", item.score)
    }

    @Test
    fun eachReleaseStatusMapsToItsUiCounterpart() {
        //Given
        val contentsByStatus = ReleaseStatusDomain.entries.associateWith { status ->
            SectionContentDomain(listItems = listOf(listItem(FIRST_ID, releaseStatus = status)))
        }

        //When
        val uiStatuses = contentsByStatus.mapValues { (_, content) ->
            renderedItems(content).single().releaseStatus
        }

        //Then
        assertEquals(
            mapOf(
                ReleaseStatusDomain.UNKNOWN to ReleaseStatusUi.UNKNOWN,
                ReleaseStatusDomain.ONGOING to ReleaseStatusUi.ONGOING,
                ReleaseStatusDomain.ANNOUNCED to ReleaseStatusUi.ANNOUNCED,
                ReleaseStatusDomain.RELEASED to ReleaseStatusUi.RELEASED
            ),
            uiStatuses
        )
    }

    @Test
    fun onlyItemsWithAnEnabledNotificationRenderItAsEnabled() {
        //Given
        val content = SectionContentDomain(
            listItems = listOf(listItem(FIRST_ID), listItem(SECOND_ID))
        )

        //When
        val items = renderedItems(content, enabledNotificationIds = setOf(FIRST_ID))

        //Then
        assertEquals(
            listOf(NotificationUi.ENABLED, NotificationUi.DISABLED),
            items.map { it.notification }
        )
    }
}
