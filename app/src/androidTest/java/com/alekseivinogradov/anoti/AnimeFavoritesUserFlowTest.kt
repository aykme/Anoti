package com.alekseivinogradov.anoti

import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.ParcelFileDescriptor
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.Res as base_Res
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.episodes
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.notifications_turn_off_description
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.notifications_turn_on_description
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.ongoing
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.score_image_description
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.Res as favorites_Res
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.empty_list
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.extra_info_on_description
import com.alekseivinogradov.anoti.main.R as main_R
import com.alekseivinogradov.anoti.main.impl.presentation.MainActivity
import com.alekseivinogradov.anoti.testutils.android.safeComposeInteraction
import com.alekseivinogradov.anoti.testutils.android.safeInteraction
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AnimeFavoritesUserFlowTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = if (Build.VERSION.SDK_INT >= TIRAMISU) {
        GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)
    } else {
        GrantPermissionRule.grant()
    }

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private suspend fun notificationButtonTurnOnDescription() =
        getString(base_Res.string.notifications_turn_on_description)
    private suspend fun notificationButtonTurnOffDescription() =
        getString(base_Res.string.notifications_turn_off_description)
    private suspend fun ongoingStatusText() = getString(base_Res.string.ongoing)

    @Before
    fun setup() {
        // Animations race with Espresso sync and cause flakiness; disable them.
        // Font scale/density can reflow list items and misdirect clicks; reset to defaults.
        runShellCommand("settings put global window_animation_scale 0")
        runShellCommand("settings put global transition_animation_scale 0")
        runShellCommand("settings put global animator_duration_scale 0")
        runShellCommand("settings put system font_scale 1.0")
        runShellCommand("wm density reset")
    }

    private fun runShellCommand(command: String) {
        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
        ParcelFileDescriptor.AutoCloseInputStream(uiAutomation.executeShellCommand(command))
            .use { it.readBytes() }
    }

    // runBlocking, not runTest: the retries below need real delays, not virtual time.
    @Test
    fun addOngoingToAnimeFavorites(): Unit = runBlocking {
        // Given
        val rvPosition = 0
        val expectedScoreImageDescription = getString(base_Res.string.score_image_description)
        val expectedExtraInfoButtonDescription =
            getString(favorites_Res.string.extra_info_on_description)
        val expectedAvailableEpisodesInfoStartWithText = getString(base_Res.string.episodes)
        val expectedReleaseStatusText = ongoingStatusText()
        val expectedNotificationButtonDescription = notificationButtonTurnOffDescription()

        // When
        goToOngoingSection()
        checkNotificationButtonIsTurnedOff(rvPosition)
        clickOnNotificationButtonInAnimeList(rvPosition)
        checkNotificationButtonIsTurnedOn(rvPosition)
        goToAnimeFavorites()

        // Then
        // "New episode" isn't checked: it reflects live backend data, not test-controlled state.
        safeComposeInteraction {
            // useUnmergedTree: hasAnyDescendant can't see Text merged into the clickable root.
            composeRule.onAllNodesWithTag("anime_favorites_item", useUnmergedTree = true)[rvPosition]
                .assertIsDisplayed()
                .assert(hasAnyDescendant(hasContentDescription(expectedScoreImageDescription)))
                .assert(
                    hasAnyDescendant(hasContentDescription(expectedExtraInfoButtonDescription))
                )
                .assert(
                    hasAnyDescendant(
                        hasTextStartingWith(expectedAvailableEpisodesInfoStartWithText)
                    )
                )
                .assert(hasAnyDescendant(hasText(expectedReleaseStatusText)))
                .assert(
                    hasAnyDescendant(hasContentDescription(expectedNotificationButtonDescription))
                )
        }
    }

    // runBlocking, not runTest: the retries below need real delays, not virtual time.
    @Test
    fun removeOngoingFromAnimeFavorites(): Unit = runBlocking {
        // Given
        val rvPosition = 0

        // When
        goToOngoingSection()
        checkNotificationButtonIsTurnedOff(rvPosition)
        clickOnNotificationButtonInAnimeList(rvPosition)
        checkNotificationButtonIsTurnedOn(rvPosition)
        goToAnimeFavorites()

        val expectedReleaseStatusText = ongoingStatusText()
        val expectedNotificationButtonDescription = notificationButtonTurnOffDescription()
        safeComposeInteraction {
            // useUnmergedTree: hasAnyDescendant can't see Text merged into the clickable root.
            composeRule.onAllNodesWithTag("anime_favorites_item", useUnmergedTree = true)[rvPosition]
                .assertIsDisplayed()
                .assert(hasAnyDescendant(hasText(expectedReleaseStatusText)))
                .assert(
                    hasAnyDescendant(hasContentDescription(expectedNotificationButtonDescription))
                )
        }

        clickOnNotificationButtonInAnimeFavorites(rvPosition)

        // Then
        // Removing the only favorite empties the list, which switches the screen to EmptyState.
        val expectedEmptyListText = getString(favorites_Res.string.empty_list)
        // assertIsDisplayed() must be inside the retry lambda: onNodeWithText() alone never
        // throws, so a bare call outside safeComposeInteraction would never actually retry.
        safeComposeInteraction {
            composeRule.onNodeWithText(expectedEmptyListText).assertIsDisplayed()
        }
        composeRule.onAllNodesWithTag("anime_favorites_item").assertCountEquals(0)
    }

    private suspend fun goToOngoingSection() {
        safeComposeInteraction {
            composeRule.onNodeWithTag("ongoing_button")
        }.performClick()
    }

    private suspend fun checkNotificationButtonIsTurnedOff(rvPosition: Int) {
        val expectedDescription = notificationButtonTurnOnDescription()
        safeComposeInteraction {
            composeRule.onAllNodesWithTag("anime_list_item", useUnmergedTree = true)[rvPosition]
                .assertIsDisplayed()
                .assert(hasAnyDescendant(hasContentDescription(expectedDescription)))
        }
    }

    private suspend fun checkNotificationButtonIsTurnedOn(rvPosition: Int) {
        val expectedDescription = notificationButtonTurnOffDescription()
        safeComposeInteraction {
            composeRule.onAllNodesWithTag("anime_list_item", useUnmergedTree = true)[rvPosition]
                .assertIsDisplayed()
                .assert(hasAnyDescendant(hasContentDescription(expectedDescription)))
        }
    }

    private suspend fun clickOnNotificationButtonInAnimeList(rvPosition: Int) {
        safeComposeInteraction {
            composeRule
                .onAllNodesWithTag("anime_list_item", useUnmergedTree = true)[rvPosition]
                .onChildren()
                .filterToOne(hasTestTag("notification_button"))
        }.performClick()
    }

    private suspend fun clickOnNotificationButtonInAnimeFavorites(rvPosition: Int) {
        safeComposeInteraction {
            composeRule
                .onAllNodesWithTag("anime_favorites_item")[rvPosition]
                .onChildren()
                .filterToOne(hasTestTag("notification_button"))
        }.performClick()
    }

    private suspend fun goToAnimeFavorites() {
        safeInteraction {
            onView(withId(main_R.id.anime_favorites)).perform(click())
        }
    }

    // hasText's substring mode is "contains"; this replicates Hamcrest's startsWith exactly.
    private fun hasTextStartingWith(prefix: String): SemanticsMatcher =
        SemanticsMatcher("${SemanticsProperties.Text.name} starts with '$prefix'") { node ->
            node.config.getOrNull(SemanticsProperties.Text)
                ?.any { it.text.startsWith(prefix) } == true
        }
}
