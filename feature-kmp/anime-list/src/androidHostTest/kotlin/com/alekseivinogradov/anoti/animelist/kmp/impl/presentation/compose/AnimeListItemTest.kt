package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.announced
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.episodes
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.notifications_turn_off_description
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.ongoing
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.released
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.ListItemUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.EpisodesInfoTypeUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.NotificationUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.ReleaseStatusUi
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.extra_episodes_info_description
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.next_episode
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose.AnotiTheme
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.Res as BaseRes

// One function per case under test, plus the helpers those cases share.
@Suppress("TooManyFunctions")
@RunWith(RobolectricTestRunner::class)
class AnimeListItemTest {

    @get:Rule
    val composeRule = createComposeRule()

    private object PassThroughDateFormatter : DateFormatter {
        override fun getFormattedDate(inputText: String, fallbackText: String): String = inputText
    }

    private val baseItem = ListItemUi(
        id = 1,
        name = "Frieren",
        imageUrl = null,
        episodesInfoType = EpisodesInfoTypeUi.AVAILABLE,
        episodesAired = 5,
        episodesTotal = 12,
        nextEpisodeAt = "2024-01-05",
        airedOn = null,
        releasedOn = null,
        score = "8.90",
        releaseStatus = ReleaseStatusUi.ONGOING,
        notification = NotificationUi.ENABLED
    )

    private val itemState = mutableStateOf(baseItem)

    // Null means the full emulated screen width. A narrow case has to be forced with
    // requiredWidth: AnotiTheme's Surface passes its own width down as a minimum, which an
    // ordinary widthIn(max) below it cannot go under.
    private val forcedWidthState = mutableStateOf<Dp?>(null)

    private var episodesInfoClicks = 0

    private var notificationClicks = 0

    /** What the item's bottom row shows at one particular item width. */
    private class BottomRowSnapshot(
        val nameShown: Boolean,
        val scoreShown: Boolean,
        val statusShown: Boolean,
        val notificationShown: Boolean,
        val notificationWidth: Dp
    )

    private fun setItem() {
        composeRule.setContent {
            AnotiTheme {
                val forcedWidth = forcedWidthState.value
                Box(
                    if (forcedWidth != null) {
                        Modifier.requiredWidth(forcedWidth)
                    } else {
                        Modifier.fillMaxWidth()
                    }
                ) {
                    AnimeListItem(
                        item = itemState.value,
                        dateFormatter = PassThroughDateFormatter,
                        onEpisodesInfoClick = { episodesInfoClicks++ },
                        onNotificationClick = { notificationClicks++ }
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    // The counts are joined by non-breaking spaces, so the line can only ever wrap after the
    // label.
    private fun availableEpisodesText(): String {
        val episodesLabel = runBlocking { getString(BaseRes.string.episodes) }
        return "$episodesLabel: 5\u00A0/\u00A012"
    }

    private fun showStatus(releaseStatus: ReleaseStatusUi) {
        itemState.value = baseItem.copy(releaseStatus = releaseStatus)
        composeRule.waitForIdle()
    }

    private fun shownLabels(labels: List<String>): List<String> {
        return labels.filter {
            composeRule.onAllNodesWithText(it).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun bottomRowSnapshot(statusLabel: String, notificationDescription: String) =
        BottomRowSnapshot(
            nameShown = composeRule.onNodeWithText(baseItem.name).isDisplayed(),
            scoreShown = composeRule.onNodeWithText(baseItem.score).isDisplayed(),
            statusShown = composeRule.onNodeWithText(statusLabel).isDisplayed(),
            notificationShown = composeRule
                .onNodeWithContentDescription(notificationDescription)
                .isDisplayed(),
            notificationWidth = composeRule
                .onNodeWithTag(NOTIFICATION_BUTTON_TAG)
                .getUnclippedBoundsInRoot()
                .width
        )

    @Test
    fun theItemShowsItsNameAndScore() {
        //Given
        itemState.value = baseItem

        //When
        setItem()

        //Then
        composeRule.onNodeWithText("Frieren").assertIsDisplayed()
        composeRule.onNodeWithText("8.90").assertIsDisplayed()
    }

    @Test
    fun theAvailableVariantShowsTheAiredAndTotalEpisodeCounts() {
        //Given
        itemState.value = baseItem.copy(episodesInfoType = EpisodesInfoTypeUi.AVAILABLE)

        //When
        setItem()

        //Then
        composeRule.onNodeWithText(availableEpisodesText()).assertIsDisplayed()
    }

    @Test
    fun theExtraVariantShowsTheNextEpisodeDateInsteadOfTheEpisodeCounts() {
        //Given
        val nextEpisodeLabel = runBlocking { getString(Res.string.next_episode) }
        itemState.value = baseItem.copy(episodesInfoType = EpisodesInfoTypeUi.EXTRA)

        //When
        setItem()

        //Then
        composeRule.onNodeWithText("$nextEpisodeLabel:\n2024-01-05").assertIsDisplayed()
        composeRule.onNodeWithText(availableEpisodesText()).assertDoesNotExist()
    }

    @Test
    fun eachReleaseStatusShowsItsOwnLabelAndAnUnknownOneShowsNone() {
        //Given
        val ongoingLabel = runBlocking { getString(BaseRes.string.ongoing) }
        val announcedLabel = runBlocking { getString(BaseRes.string.announced) }
        val releasedLabel = runBlocking { getString(BaseRes.string.released) }
        val allLabels = listOf(ongoingLabel, announcedLabel, releasedLabel)
        setItem()

        //When
        showStatus(ReleaseStatusUi.ONGOING)
        val whileOngoing = shownLabels(allLabels)
        showStatus(ReleaseStatusUi.ANNOUNCED)
        val whileAnnounced = shownLabels(allLabels)
        showStatus(ReleaseStatusUi.RELEASED)
        val whileReleased = shownLabels(allLabels)
        showStatus(ReleaseStatusUi.UNKNOWN)
        val whileUnknown = shownLabels(allLabels)

        //Then
        assertEquals(listOf(ongoingLabel), whileOngoing)
        assertEquals(listOf(announcedLabel), whileAnnounced)
        assertEquals(listOf(releasedLabel), whileReleased)
        assertEquals(emptyList(), whileUnknown)
        composeRule.onNodeWithText("Frieren").assertIsDisplayed()
    }

    @Test
    fun theNotificationToggleReportsItsClick() {
        //Given
        itemState.value = baseItem.copy(notification = NotificationUi.ENABLED)
        setItem()

        //When
        composeRule.onNodeWithTag(NOTIFICATION_BUTTON_TAG).performClick()
        composeRule.waitForIdle()

        //Then
        assertEquals(1, notificationClicks)
        assertEquals(0, episodesInfoClicks)
    }

    @Test
    fun theEpisodesInfoToggleReportsItsClick() {
        //Given
        val toggleDescription = runBlocking {
            getString(Res.string.extra_episodes_info_description)
        }
        itemState.value = baseItem.copy(episodesInfoType = EpisodesInfoTypeUi.AVAILABLE)
        setItem()

        //When
        composeRule.onNodeWithContentDescription(toggleDescription).performClick()
        composeRule.waitForIdle()

        //Then
        assertEquals(1, episodesInfoClicks)
        assertEquals(0, notificationClicks)
    }

    @Test
    fun aWideAndANarrowItemBothRenderEveryPartOfTheBottomRow() {
        //Given
        val ongoingLabel = runBlocking { getString(BaseRes.string.ongoing) }
        val notificationDescription = runBlocking {
            getString(BaseRes.string.notifications_turn_off_description)
        }
        forcedWidthState.value = null
        setItem()

        //When
        val wide = bottomRowSnapshot(ongoingLabel, notificationDescription)
        forcedWidthState.value = NARROW_ITEM_WIDTH_DP.dp
        composeRule.waitForIdle()
        val narrow = bottomRowSnapshot(ongoingLabel, notificationDescription)

        //Then
        listOf("wide" to wide, "narrow" to narrow).forEach { (label, snapshot) ->
            assertTrue(snapshot.nameShown, "the $label item hides its name")
            assertTrue(snapshot.scoreShown, "the $label item hides its score")
            assertTrue(snapshot.statusShown, "the $label item hides its release status")
            assertTrue(snapshot.notificationShown, "the $label item hides its notification toggle")
        }
        assertEquals(
            wide.notificationWidth,
            narrow.notificationWidth,
            "the notification toggle must keep its full size however narrow the item gets"
        )
    }
}

private const val NOTIFICATION_BUTTON_TAG = "notification_button"

// Narrower than the score/status/notification chain needs, so the status text has to give up
// part of its natural width.
private const val NARROW_ITEM_WIDTH_DP = 260
