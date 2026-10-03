package com.alekseivinogradov.anoti.main.impl.presentation

import android.os.Bundle
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(application = HostApplicationFake::class)
class MainActivityNotificationTapTest {

    private val mainDispatcher = TestMainDispatcher()

    // The activity builds the compose content itself, so the rule only tracks the composition
    // and never launches anything. It shares the clock the stores run on.
    @get:Rule
    val composeRule = createEmptyComposeRule(StandardTestDispatcher(mainDispatcher.scheduler))

    @BeforeTest
    fun installTestDispatcher() = mainDispatcher.install()

    @AfterTest
    fun removeTestDispatcher() {
        mainDispatcher.remove()
        assertNoAnimeDetailsRequested()
    }

    @Test
    fun aTapOnTheListOpensFavorites() {
        //Given
        val controller = composeRule.launchMainActivity(plainLaunchingIntent())
        val listScreenStore = fakeDependencies.animeDatabaseStores.last()

        //When
        controller.newIntent(favoritesDeepLinkIntent())
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithTag(ANIME_FAVORITES_TAB_TAG).assertIsSelected()
        assertTrue(listScreenStore.isDisposed)
    }

    @Test
    fun aTapOnFavoritesKeepsTheScreenItShows() {
        //Given
        val controller = composeRule.launchMainActivity(favoritesDeepLinkIntent())
        val favoritesScreenStore = fakeDependencies.animeDatabaseStores.last()
        val storesBefore = fakeDependencies.animeDatabaseStores.size

        //When
        controller.newIntent(favoritesDeepLinkIntent())
        composeRule.waitForIdle()

        //Then
        assertFalse(favoritesScreenStore.isDisposed)
        assertEquals(storesBefore, fakeDependencies.animeDatabaseStores.size)
        composeRule.onNodeWithTag(ANIME_FAVORITES_TAB_TAG).assertIsSelected()
    }

    @Test
    fun aTapInTheBackgroundShowsFavoritesOnceTheAppIsBack() {
        //Given
        val controller = composeRule.launchMainActivity(plainLaunchingIntent())
        controller.pause().stop()

        //When
        controller.newIntent(favoritesDeepLinkIntent())
        controller.restart().resume()
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithTag(ANIME_FAVORITES_TAB_TAG).assertIsSelected()
    }

    @Test
    fun aTapIntoAnActivityRebuiltFromSavedStateRestoresThenOpensFavorites() {
        //Given
        val first = composeRule.launchMainActivity(plainLaunchingIntent())
        val saved = Bundle()
        first.saveInstanceState(saved).pause().stop().destroy()
        val second = Robolectric.buildActivity(MainActivity::class.java, plainLaunchingIntent())
            .create(saved).start().restoreInstanceState(saved).postCreate(saved)
        val restoredListStore = fakeDependencies.animeDatabaseStores.last()

        //When
        second.newIntent(favoritesDeepLinkIntent())
        second.resume().visible()
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithTag(ANIME_FAVORITES_TAB_TAG).assertIsSelected()
        assertTrue(restoredListStore.isDisposed)
    }

    @Test
    fun aTapIntoARebuiltActivityLeavesNoStaleSearchTextOnTheList() {
        //Given
        val first = composeRule.launchMainActivity(plainLaunchingIntent())
        composeRule.onNodeWithTag(SEARCH_BUTTON_TAG).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasSetTextAction()).performTextInput(SEARCH_TEXT)
        composeRule.waitForIdle()
        val saved = Bundle()
        first.saveInstanceState(saved).pause().stop().destroy()
        val second = Robolectric.buildActivity(MainActivity::class.java, plainLaunchingIntent())
            .create(saved).start().restoreInstanceState(saved).postCreate(saved)
        second.newIntent(favoritesDeepLinkIntent())
        second.resume().visible()
        composeRule.waitForIdle()

        //When
        composeRule.onNodeWithTag(ANIME_LIST_TAB_TAG).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(SEARCH_BUTTON_TAG).performClick()
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithText(SEARCH_TEXT).assertDoesNotExist()
    }

    @Test
    fun aTapNamingNoScreenLeavesTheScreenAlone() {
        //Given
        val controller = composeRule.launchMainActivity(favoritesDeepLinkIntent())
        val favoritesScreenStore = fakeDependencies.animeDatabaseStores.last()

        //When
        controller.newIntent(plainLaunchingIntent())
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithTag(ANIME_FAVORITES_TAB_TAG).assertIsSelected()
        assertFalse(favoritesScreenStore.isDisposed)
    }

    @Test
    fun aTapWithAnUnreadablePayloadLeavesTheScreenAlone() {
        //Given
        val controller = composeRule.launchMainActivity(favoritesDeepLinkIntent())
        val favoritesScreenStore = fakeDependencies.animeDatabaseStores.last()

        //When
        controller.newIntent(
            plainLaunchingIntent().putExtra(MainActivity.EXTRA_DEEP_LINK_TARGET, "not a screen")
        )
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithTag(ANIME_FAVORITES_TAB_TAG).assertIsSelected()
        assertFalse(favoritesScreenStore.isDisposed)
    }

    @Test
    fun aTapDoesNotRunThePermissionCheckAgain() {
        //Given
        expectAnExplanation()
        val controller = composeRule.launchMainActivity(plainLaunchingIntent())
        composeRule.onNodeWithText(REFUSE_LABEL).performClick()
        composeRule.waitForIdle()

        //When
        controller.newIntent(favoritesDeepLinkIntent())
        composeRule.waitForIdle()

        //Then
        composeRule.onNode(isDialog()).assertDoesNotExist()
    }
}
