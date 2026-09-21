package com.alekseivinogradov.anoti.main.impl.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(application = FakeHostApplication::class)
class MainActivityNavigationTest {

    private val mainDispatcher = TestMainDispatcher()

    // The activity builds the compose content itself, so the rule only tracks the composition
    // and never launches anything. It shares the clock the stores run on.
    @get:Rule
    val composeRule = createEmptyComposeRule(StandardTestDispatcher(mainDispatcher.scheduler))

    @BeforeTest
    fun installTestDispatcher() = mainDispatcher.install()

    @AfterTest
    fun removeTestDispatcher() = mainDispatcher.remove()

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
    fun staysPutWhenTheTabAlreadyOpenIsTappedAgain() {
        //Given
        composeRule.launchMainActivity(plainLaunchingIntent())
        val screenBefore = fakeDependencies.animeDatabaseStores.last()

        //When
        composeRule.onNodeWithTag(ANIME_LIST_TAB_TAG).performClick()
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithTag(ANIME_LIST_TAB_TAG).assertIsSelected()
        composeRule.onNodeWithTag(ANIME_FAVORITES_TAB_TAG).assertIsNotSelected()
        assertFalse(screenBefore.isDisposed)
    }

    @Test
    fun keepsTheScreenYouWalkedToWhenItIsRebuiltFromSavedState() {
        //Given
        val controller = composeRule.launchMainActivity(favoritesDeepLinkIntent())
        composeRule.onNodeWithTag(ANIME_LIST_TAB_TAG).performClick()
        composeRule.waitForIdle()

        //When
        controller.recreate()
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithTag(ANIME_LIST_TAB_TAG).assertIsSelected()
        composeRule.onNodeWithTag(ANIME_FAVORITES_TAB_TAG).assertIsNotSelected()
    }

    @Test
    fun countsTheSavedAnimeWithANewEpisodeOnTheFavoritesTab() {
        //Given
        composeRule.launchMainActivity(plainLaunchingIntent())
        // The bar's own store: the host takes it before the first screen takes its own.
        val barDatabase = fakeDependencies.animeDatabaseStores.first()

        //When
        barDatabase.emit(
            listOf(
                savedAnime(id = 1, hasNewEpisode = true),
                savedAnime(id = 2, hasNewEpisode = false),
                savedAnime(id = 3, hasNewEpisode = true)
            )
        )
        composeRule.waitForIdle()

        //Then
        // The tab merges its children's semantics, so the badge is only its own node unmerged.
        composeRule.onNodeWithText("2", useUnmergedTree = true).assertIsDisplayed()
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
