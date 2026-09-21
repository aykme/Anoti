package com.alekseivinogradov.anoti.main.impl.presentation

import android.Manifest
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(application = FakeHostApplication::class)
class MainActivityStartupTest {

    private val dispatcher = TestDispatcherRule()

    // The activity builds the compose content itself, and each test needs its own launching
    // intent, so the rule only tracks the composition and never launches anything. It shares the
    // clock the stores run on.
    @get:Rule
    val composeRule = createEmptyComposeRule(StandardTestDispatcher(dispatcher.scheduler))

    @BeforeTest
    fun installTestDispatcher() = dispatcher.install()

    @AfterTest
    fun removeTestDispatcher() = dispatcher.remove()

    @Test
    fun opensOnTheAnimeListWhenNothingAsksForAnotherScreen() {
        //Given
        val intent = plainLaunchingIntent()

        //When
        composeRule.launchMainActivity(intent)

        //Then
        composeRule.onNodeWithTag(ANIME_LIST_TAB_TAG).assertIsSelected()
        composeRule.onNodeWithTag(ANIME_FAVORITES_TAB_TAG).assertIsNotSelected()
    }

    @Test
    fun opensOnFavoritesWhenTheLaunchingIntentAsksForIt() {
        //Given
        val intent = plainLaunchingIntent().putExtra(
            MainActivity.EXTRA_DEEP_LINK_TARGET,
            Json.encodeToString(NavRootConfig.serializer(), NavRootConfig.AnimeFavorites)
        )

        //When
        composeRule.launchMainActivity(intent)

        //Then
        composeRule.onNodeWithTag(ANIME_FAVORITES_TAB_TAG).assertIsSelected()
        composeRule.onNodeWithTag(ANIME_LIST_TAB_TAG).assertIsNotSelected()
    }

    @Test
    fun fallsBackToTheAnimeListWhenThePayloadCannotBeRead() {
        //Given
        val intent = plainLaunchingIntent()
            .putExtra(MainActivity.EXTRA_DEEP_LINK_TARGET, "not a payload")

        //When
        composeRule.launchMainActivity(intent)

        //Then
        composeRule.onNodeWithTag(ANIME_LIST_TAB_TAG).assertIsSelected()
    }

    @Test
    fun asksForNotificationPermissionOnAFreshStart() {
        //Given
        val intent = plainLaunchingIntent()

        //When
        val controller = composeRule.launchMainActivity(intent)

        //Then
        assertEquals(
            Manifest.permission.POST_NOTIFICATIONS,
            shadowOf(controller.get()).lastRequestedPermission?.requestedPermissions?.single()
        )
    }

    @Test
    fun staysQuietWhenNotificationPermissionIsAlreadyGranted() {
        //Given
        shadowOf(RuntimeEnvironment.getApplication())
            .grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        //When
        val controller = composeRule.launchMainActivity(plainLaunchingIntent())

        //Then
        assertEquals(null, shadowOf(controller.get()).lastRequestedPermission)
    }
}
