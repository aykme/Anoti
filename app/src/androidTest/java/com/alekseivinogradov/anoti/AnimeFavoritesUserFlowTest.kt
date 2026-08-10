package com.alekseivinogradov.anoti

import android.content.res.Resources
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.ParcelFileDescriptor
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.alekseivinogradov.anoti.animefavorites.kmp.R as anime_favorites_R
import com.alekseivinogradov.anoti.animelist.kmp.R as anime_list_R
import com.alekseivinogradov.anoti.main.R as main_R
import com.alekseivinogradov.anoti.main.impl.presentation.MainActivity
import com.alekseivinogradov.anoti.testutils.platform.action.clickOnChildView
import com.alekseivinogradov.anoti.testutils.platform.matcher.AtRecyclerPositionMatcher
import com.alekseivinogradov.anoti.testutils.platform.safeInteraction
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.startsWith
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
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var resources: Resources
    private val notificationButtonTurnOnDescription
        get() = resources.getString(anime_favorites_R.string.notifications_turn_on_description)
    private val notificationButtonTurnOffDescription
        get() = resources.getString(anime_favorites_R.string.notifications_turn_off_description)
    private val ongoingStatusText get() = anime_favorites_R.string.ongoing

    @Before
    fun setup() {
        resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        // Real-device animations (item removal, ripples, transitions) race with Espresso's
        // synchronization and are a known source of instrumented-test flakiness; disable them.
        // A device-wide font scale/density override (e.g. large accessibility text) reflows
        // list items enough to overlap other views, which can make a child-view click land on
        // stale/misbound content; reset both to defaults so the test doesn't depend on whatever
        // the device happens to be configured with.
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

    // "Run Blocking" because of don't need to skip delay with interaction retry
    @Test
    fun addOngoingToAnimeFavorites() = runBlocking {
        // Given
        val rvPosition = 0
        val expectedNewEpisodeText =
            resources.getString(anime_favorites_R.string.new_episode)
        val expectedScoreImageDescription =
            resources.getString(anime_favorites_R.string.score_image_description)
        val expectedExtraInfoButtonDescription =
            resources.getString(anime_favorites_R.string.extra_info_on_description)
        val expectedAvailableEpisodesInfoStartWithText =
            resources.getString(anime_favorites_R.string.episodes)
        val expectedReleaseStatusText = ongoingStatusText
        val expectedNotificationButtonDescription = notificationButtonTurnOffDescription

        // When
        goToOngoingSection()

        // Notification button in "Anime list" must be turned off before click
        checkNotificationButtonIsTurnedOff(rvPosition)

        // Turn on notifications for the first element in "Anime list"
        clickOnNotificationButtonInAnimeList(rvPosition)

        // Notification button in "Anime list" must be turned on after click
        checkNotificationButtonIsTurnedOn(rvPosition)

        // Go to "Anime favorites"
        goToAnimeFavorites()

        // Then
        // Check the first element of a Recycler View was added and valid
        safeInteraction {
            onView(withId(anime_favorites_R.id.anime_favorites_rv))
                .check(
                    matches(
                        AtRecyclerPositionMatcher(
                            position = rvPosition,
                            viewMather = allOf(
                                isDisplayed(),
                                withId(anime_favorites_R.id.item_anime_favorites_layout),
                                hasDescendant(withId(anime_favorites_R.id.new_episode_background)),
                                hasDescendant(withId(anime_favorites_R.id.new_episode_text)),
                                hasDescendant(withText(expectedNewEpisodeText)),
                                hasDescendant(withId(anime_favorites_R.id.image_info_background)),
                                hasDescendant(withId(anime_favorites_R.id.score_image)),
                                hasDescendant(withContentDescription(expectedScoreImageDescription)),
                                hasDescendant(withId(anime_favorites_R.id.info_type_button)),
                                hasDescendant(
                                    withContentDescription(expectedExtraInfoButtonDescription)
                                ),
                                hasDescendant(withId(anime_favorites_R.id.main_info_stroke)),
                                hasDescendant(withId(anime_favorites_R.id.main_info_background)),
                                hasDescendant(withId(anime_favorites_R.id.name_text)),
                                hasDescendant(
                                    withId(anime_favorites_R.id.available_episodes_info_text)
                                ),
                                hasDescendant(
                                    withText(startsWith(expectedAvailableEpisodesInfoStartWithText))
                                ),
                                hasDescendant(withId(anime_favorites_R.id.release_status_text)),
                                hasDescendant(withText(expectedReleaseStatusText)),
                                hasDescendant(withId(anime_favorites_R.id.notification_button)),
                                hasDescendant(
                                    withContentDescription(expectedNotificationButtonDescription)
                                )
                            )
                        )
                    )
                )
        }

        Unit
    }

    // "Run Blocking" because of don't need to skip delay with interaction retry
    @Test
    fun removeOngoingFromAnimeFavorites() = runBlocking {
        // Given
        val rvPosition = 0

        // When
        goToOngoingSection()

        // Notification button in "Anime list" must be turned off before click
        checkNotificationButtonIsTurnedOff(rvPosition)

        // Turn on notifications for the first element in "Anime list"
        clickOnNotificationButtonInAnimeList(rvPosition)

        // Notification button in "Anime list" must be turned on after click
        checkNotificationButtonIsTurnedOn(rvPosition)

        // Go to "Anime favorites"
        goToAnimeFavorites()

        // Check that ongoing was added to "Anime favorites":
        // It is displayed, has "ongoing" status text,
        // and description for turned on notification button
        safeInteraction {
            onView(withId(anime_favorites_R.id.anime_favorites_rv))
                .check(
                    matches(
                        AtRecyclerPositionMatcher(
                            position = rvPosition,
                            viewMather = allOf(
                                isDisplayed(),
                                hasDescendant(withText(ongoingStatusText)),
                                hasDescendant(
                                    withContentDescription(notificationButtonTurnOffDescription)
                                )
                            )
                        )
                    )
                )
        }

        // Turn off notifications for the first element in "Anime favorites"
        clickOnNotificationButtonInAnimeFavorites(rvPosition)

        // Then
        // Check the first element of a Recycler View was removed
        safeInteraction {
            onView(withId(anime_favorites_R.id.anime_favorites_rv))
                .check(
                    matches(
                        AtRecyclerPositionMatcher(
                            position = rvPosition,
                            viewMather = allOf(
                                not(isDisplayed()),
                                not(withId(anime_favorites_R.id.item_anime_favorites_layout)),
                            )
                        )
                    )
                )
        }

        Unit
    }

    private suspend fun goToOngoingSection() {
        safeInteraction {
            onView(withId(anime_list_R.id.ongoing_button))
                .perform(click())
        }
    }

    private suspend fun checkNotificationButtonIsTurnedOff(rvPosition: Int) {
        safeInteraction {
            onView(withId(anime_list_R.id.anime_list_rv))
                .check(
                    matches(
                        AtRecyclerPositionMatcher(
                            position = rvPosition,
                            viewMather = hasDescendant(
                                withContentDescription(notificationButtonTurnOnDescription)
                            )
                        )
                    )
                )
        }
    }

    private suspend fun checkNotificationButtonIsTurnedOn(rvPosition: Int) {
        safeInteraction {
            onView(withId(anime_list_R.id.anime_list_rv))
                .check(
                    matches(
                        AtRecyclerPositionMatcher(
                            position = rvPosition,
                            viewMather = hasDescendant(
                                withContentDescription(notificationButtonTurnOffDescription)
                            )
                        )
                    )
                )
        }
    }

    private suspend fun clickOnNotificationButtonInAnimeList(rvPosition: Int) {
        safeInteraction {
            onView(withId(anime_list_R.id.anime_list_rv))
                .perform(
                    actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        /* position = */ rvPosition,
                        /* viewAction = */ clickOnChildView(anime_list_R.id.notification_button)
                    )
                )
        }
    }

    private suspend fun clickOnNotificationButtonInAnimeFavorites(rvPosition: Int) {
        safeInteraction {
            onView(withId(anime_favorites_R.id.anime_favorites_rv))
                .perform(
                    actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        /* position = */ rvPosition,
                        /* viewAction = */ clickOnChildView(anime_favorites_R.id.notification_button)
                    )
                )
        }
    }

    private suspend fun goToAnimeFavorites() {
        safeInteraction {
            onView(withId(main_R.id.anime_favorites)).perform(click())
        }
    }
}
