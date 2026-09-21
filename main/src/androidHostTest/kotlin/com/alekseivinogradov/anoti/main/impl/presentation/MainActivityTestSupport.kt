package com.alekseivinogradov.anoti.main.impl.presentation

import android.content.Intent
import androidx.compose.ui.test.junit4.ComposeTestRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.android.controller.ActivityController

internal const val ANIME_LIST_TAB_TAG = "anime_list_button"
internal const val ANIME_FAVORITES_TAB_TAG = "anime_favorites_button"

/** The fakes the activity under test is wired to, reachable from a test that never built them. */
internal val fakeDependencies: FakeDiRootDependencies
    get() = checkNotNull(RuntimeEnvironment.getApplication() as? FakeHostApplication) {
        "The test must run with FakeHostApplication."
    }.dependencies

/** What the launcher sends, with no deep link on it. */
internal fun plainLaunchingIntent(): Intent =
    Intent(RuntimeEnvironment.getApplication(), MainActivity::class.java)

/**
 * One virtual clock for the composition and for the stores alike, so nothing in a test waits on
 * real time or on a second thread.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class TestDispatcherRule {

    val scheduler = TestCoroutineScheduler()

    fun install() {
        Dispatchers.setMain(StandardTestDispatcher(scheduler))
    }

    fun remove() {
        Dispatchers.resetMain()
    }
}

/** Takes the activity all the way to resumed and lets the first composition settle. */
internal fun ComposeTestRule.launchMainActivity(
    intent: Intent
): ActivityController<MainActivity> =
    Robolectric.buildActivity(MainActivity::class.java, intent).setup().also { waitForIdle() }
