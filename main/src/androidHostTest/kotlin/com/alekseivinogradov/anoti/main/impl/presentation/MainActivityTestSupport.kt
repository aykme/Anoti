package com.alekseivinogradov.anoti.main.impl.presentation

import android.content.Intent
import androidx.compose.ui.test.junit4.ComposeTestRule
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.android.controller.ActivityController

internal const val ANIME_LIST_TAB_TAG = "anime_list_button"
internal const val ANIME_FAVORITES_TAB_TAG = "anime_favorites_button"

/** The fakes the activity under test is wired to, reachable from a test that never built them. */
internal val fakeDependencies: DiRootDependenciesFake
    get() = checkNotNull(RuntimeEnvironment.getApplication() as? HostApplicationFake) {
        "The test must run with HostApplicationFake."
    }.dependencies

/** What the launcher sends, with no deep link on it. */
internal fun plainLaunchingIntent(): Intent =
    Intent(RuntimeEnvironment.getApplication(), MainActivity::class.java)

/** What the new-episode notification sends: the same intent, asking for the favorites screen. */
internal fun favoritesDeepLinkIntent(): Intent =
    plainLaunchingIntent().putExtra(
        MainActivity.EXTRA_DEEP_LINK_TARGET,
        Json.encodeToString(NavRootConfig.serializer(), NavRootConfig.AnimeFavorites)
    )

/** A saved anime with only the fields the bar's badge counts on. */
internal fun savedAnime(id: Int, hasNewEpisode: Boolean) = AnimeDbDomain(
    id = id,
    imageUrl = null,
    name = "Anime $id",
    episodesAired = null,
    episodesTotal = null,
    nextEpisodeAt = null,
    airedOn = null,
    releasedOn = null,
    score = null,
    releaseStatus = ReleaseStatusDb.ONGOING,
    episodesViewed = 0,
    isNewEpisode = hasNewEpisode
)

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

/** Takes the activity all the way to resumed and lets the first composition settle. */
internal fun ComposeTestRule.launchMainActivity(
    intent: Intent
): ActivityController<MainActivity> =
    Robolectric.buildActivity(MainActivity::class.java, intent).setup().also { waitForIdle() }
