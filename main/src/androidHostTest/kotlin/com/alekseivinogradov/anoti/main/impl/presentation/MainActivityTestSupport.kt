package com.alekseivinogradov.anoti.main.impl.presentation

import android.Manifest
import android.content.Intent
import androidx.compose.ui.test.junit4.ComposeTestRule
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import com.alekseivinogradov.anoti.navigation.kmp.NavRootDeepLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import kotlin.test.assertEquals

internal const val ANIME_LIST_TAB_TAG = "anime_list_button"
internal const val ANIME_FAVORITES_TAB_TAG = "anime_favorites_button"
internal const val SEARCH_BUTTON_TAG = "search_button"
internal const val SEARCH_TEXT = "frieren"

/** The fakes the activity under test is wired to, reachable from a test that never built them. */
internal val fakeDependencies: DiRootDependenciesFake
    get() = checkNotNull(RuntimeEnvironment.getApplication() as? HostApplicationFake) {
        "The test must run with HostApplicationFake."
    }.dependencies

/** The activity's own intent with no deep link on it, as a plain launch sends it. */
internal fun plainLaunchingIntent(): Intent =
    Intent(RuntimeEnvironment.getApplication(), MainActivity::class.java)

/** The same intent asking for the favorites screen, as a tapped notification does. */
internal fun favoritesDeepLinkIntent(): Intent =
    plainLaunchingIntent().putExtra(
        MainActivity.EXTRA_DEEP_LINK_TARGET,
        NavRootDeepLink.encode(NavRootConfig.AnimeFavorites)
    )

// The dialog is drawn by another module, which ships one set of strings and keeps its resource
// accessors to itself, so its buttons can only be reached here by the words on them.
internal const val ACCEPT_LABEL = "Kawaii nya ≽^•⩊•^≼"
internal const val REFUSE_LABEL = "Angry nya ฅ^•ﻌ•^ฅ"

/** Makes the system report that the notification permission owes the user an explanation. */
internal fun expectAnExplanation() {
    shadowOf(RuntimeEnvironment.getApplication().packageManager)
        .setShouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS, true)
}

/**
 * One virtual clock for the composition and for the stores alike, so nothing in a test waits on
 * real time or on a second thread. Installed and removed by hand — it is not a JUnit rule.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class TestMainDispatcher {

    val scheduler = TestCoroutineScheduler()

    fun install() {
        Dispatchers.setMain(StandardTestDispatcher(scheduler))
    }

    fun remove() {
        Dispatchers.resetMain()
    }
}

/** Fails when a screen asked the catalog for an anime's details, which no test here opens. */
internal fun assertNoAnimeDetailsRequested() {
    assertEquals(
        listOf(),
        fakeDependencies.animeDetailsRequests,
        "a screen went to the catalog for details"
    )
}

/** Takes the activity all the way to resumed and lets the first composition settle. */
internal fun ComposeTestRule.launchMainActivity(
    intent: Intent
): ActivityController<MainActivity> =
    Robolectric.buildActivity(MainActivity::class.java, intent).setup().also { waitForIdle() }
