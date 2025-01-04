package com.alekseivinogradov.app

import android.content.res.Resources
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
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
import com.alekseivinogradov.main.impl.presentation.MainActivity
import com.alekseivinogradov.test_utils.action.clickOnChildView
import com.alekseivinogradov.test_utils.matcher.AtRecyclerPositionMatcher
import com.alekseivinogradov.test_utils.safeInteraction
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.startsWith
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.alekseivinogradov.anime_favorites_platform.R as anime_favorites_R
import com.alekseivinogradov.anime_list_platform.R as anime_list_R
import com.alekseivinogradov.main.R as main_R

class UserFlowTest {

    @get:Rule()
    val grantPermissionRule = if (Build.VERSION.SDK_INT >= TIRAMISU) {
        GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)
    } else {
        GrantPermissionRule.grant()
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var resources: Resources
    private val onAirButtonText get() = resources.getString(anime_list_R.string.on_air)

    @Before
    fun setup() {
        resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
    }


    @Test
    fun addOngoingToAnimeFavorites() = runBlocking {
        // Given
        val rvPosition = 0
        val expectedAnimeListNotificationButtonBeforeClickDescription =
            resources.getString(anime_favorites_R.string.notifications_turn_on_description)
        val expectedAnimeListNotificationButtonAfterClickDescription =
            resources.getString(anime_favorites_R.string.notifications_turn_off_description)
        val expectedNewEpisodeText =
            resources.getString(anime_favorites_R.string.new_episode)
        val expectedScoreImageDescription =
            resources.getString(anime_favorites_R.string.score_image_description)
        val expectedExtraInfoButtonDescription =
            resources.getString(anime_favorites_R.string.extra_info_on_description)
        val expectedAvailableEpisodesInfoStartWithText =
            resources.getString(anime_favorites_R.string.episodes)
        val expectedReleaseStatusText = resources.getString(anime_favorites_R.string.ongoing)
        val expectedAnimeFavoritesNotificationButtonDescription =
            resources.getString(anime_favorites_R.string.notifications_turn_off_description)

        // When
        // Go to "Ongoing" section
        safeInteraction {
            onView(withId(anime_list_R.id.ongoing_button))
                .perform(click())
        }

        // Notification button must be furn off before click
        safeInteraction {
            onView(withId(anime_list_R.id.anime_list_rv))
                .check(
                    matches(
                        AtRecyclerPositionMatcher(
                            position = rvPosition,
                            viewMather = hasDescendant(
                                withContentDescription(
                                    expectedAnimeListNotificationButtonBeforeClickDescription
                                )
                            )
                        )
                    )
                )
        }

        // Enable notifications for the first element
        safeInteraction {
            onView(withId(anime_list_R.id.anime_list_rv))
                .perform(
                    actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        /* position = */ rvPosition,
                        /* viewAction = */ clickOnChildView(anime_list_R.id.notification_button)
                    )
                )
        }

        // Notification button must be furn on before click
        safeInteraction {
            onView(withId(anime_list_R.id.anime_list_rv))
                .check(
                    matches(
                        AtRecyclerPositionMatcher(
                            position = rvPosition,
                            viewMather = hasDescendant(
                                withContentDescription(
                                    expectedAnimeListNotificationButtonAfterClickDescription
                                )
                            )
                        )
                    )
                )
        }

        // Go to "Anime favorites" section
        safeInteraction {
            onView(withId(main_R.id.anime_favorites)).perform(click())
        }

        // Then
        // Check the first element of a Recycler View
        safeInteraction {
            onView(allOf(withId(anime_favorites_R.id.anime_favorites_rv)))
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
                                    withContentDescription(
                                        expectedAnimeFavoritesNotificationButtonDescription
                                    )
                                )
                            )
                        )
                    )
                )
        }

        Unit
    }
}
