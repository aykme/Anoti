package com.alekseivinogradov.anoti.main.impl.presentation

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(application = FakeHostApplication::class)
class MainActivityNavigationTest {

    private val dispatcher = TestDispatcherRule()

    // The activity builds the compose content itself, so the rule only tracks the composition
    // and never launches anything. It shares the clock the stores run on.
    @get:Rule
    val composeRule = createEmptyComposeRule(StandardTestDispatcher(dispatcher.scheduler))

    @BeforeTest
    fun installTestDispatcher() = dispatcher.install()

    @AfterTest
    fun removeTestDispatcher() = dispatcher.remove()

    @Test
    fun switchesToFavoritesWhenItsTabIsTapped() {
        //Given
        composeRule.launchMainActivity(plainLaunchingIntent())

        //When
        composeRule.onNodeWithTag(ANIME_FAVORITES_TAB_TAG).performClick()
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithTag(ANIME_FAVORITES_TAB_TAG).assertIsSelected()
        composeRule.onNodeWithTag(ANIME_LIST_TAB_TAG).assertIsNotSelected()
    }

    @Test
    fun switchesBackToTheAnimeListWhenItsTabIsTapped() {
        //Given
        composeRule.launchMainActivity(plainLaunchingIntent())
        composeRule.onNodeWithTag(ANIME_FAVORITES_TAB_TAG).performClick()
        composeRule.waitForIdle()

        //When
        composeRule.onNodeWithTag(ANIME_LIST_TAB_TAG).performClick()
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithTag(ANIME_LIST_TAB_TAG).assertIsSelected()
        composeRule.onNodeWithTag(ANIME_FAVORITES_TAB_TAG).assertIsNotSelected()
    }

    @Test
    fun buildsNoSecondScreenWhenTheTabAlreadyOpenIsTappedAgain() {
        //Given
        composeRule.launchMainActivity(plainLaunchingIntent())
        val screensBuilt = fakeDependencies.animeDatabaseStores.size

        //When
        composeRule.onNodeWithTag(ANIME_LIST_TAB_TAG).performClick()
        composeRule.waitForIdle()

        //Then
        assertEquals(screensBuilt, fakeDependencies.animeDatabaseStores.size)
        composeRule.onNodeWithTag(ANIME_LIST_TAB_TAG).assertIsSelected()
    }

    @Test
    fun leavesNothingRunningOnTheScreenItNavigatedAwayFrom() {
        //Given
        composeRule.launchMainActivity(plainLaunchingIntent())
        val listScreenStore = fakeDependencies.animeDatabaseStores.last()

        //When
        composeRule.onNodeWithTag(ANIME_FAVORITES_TAB_TAG).performClick()
        composeRule.waitForIdle()

        //Then
        assertTrue(listScreenStore.isDisposed)
    }

    @Test
    fun leavesNothingRunningOnceTheActivityIsGone() {
        //Given
        val controller = composeRule.launchMainActivity(plainLaunchingIntent())
        val stores = fakeDependencies.animeDatabaseStores.toList()

        //When
        controller.pause().stop().destroy()
        composeRule.waitForIdle()

        //Then
        assertTrue(stores.isNotEmpty())
        assertTrue(stores.all { it.isDisposed })
    }

    @Test
    fun leavesNothingRunningWhenItDiesBeforeItEverDraws() {
        //Given
        val controller = Robolectric
            .buildActivity(MainActivity::class.java, plainLaunchingIntent())
            .create()
        val stores = fakeDependencies.animeDatabaseStores.toList()

        //When
        controller.destroy()

        //Then
        assertTrue(stores.isNotEmpty())
        assertTrue(stores.all { it.isDisposed })
    }
}
