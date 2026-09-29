package com.alekseivinogradov.anoti.animenotification.android.impl.presentation.manager

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.SystemClock
import android.service.notification.StatusBarNotification
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.alekseivinogradov.anoti.animenotification.android.impl.presentation.factory.AnimeNotificationChannelFactory
import com.alekseivinogradov.anoti.animenotification.android.impl.presentation.factory.CHANNEL_ID
import com.alekseivinogradov.anoti.animenotification.external.android.impl.presentation.provider.fake.AnimeNotificationIntentProviderFake
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.poster.PosterLoader
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.fake.CoroutineContextProviderFake
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.fail

private const val FIRST_SINGLE_ID = 10
private const val LAST_SINGLE_ID = 29
private const val SINGLE_ID_COUNT = LAST_SINGLE_ID - FIRST_SINGLE_ID + 1
private const val SUMMARY_ID = 0
private const val BURST_SIZE = 5
private const val EPISODE = 1

// Picks an order whose first id is not the lowest, so posting time and id order disagree.
private const val SHUFFLE_SEED = 7

private const val POLL_ATTEMPTS = 100
private const val POLL_STEP_MILLIS = 50L

// The service drops an update to a notification already on screen while the app averages more
// than five posts a second. Twenty quick posts push it far above that, and a second of quiet
// brings it back down.
private const val RATE_LIMIT_COOLDOWN_MILLIS = 2_000L

/**
 * Posts through the real notification service of the device the test runs on. The service
 * applies a post after `notify()` returns, lists what is on screen, and stamps each post with the
 * wall clock. Robolectric does none of that the way a device does.
 *
 * Waiting on the service is the one place these tests use real time. Each wait polls for what it
 * expects and gives up after five seconds.
 */
// One function per case under test, plus the helpers those cases share.
@Suppress("TooManyFunctions")
class AnimeNotificationIdsDeviceTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = if (Build.VERSION.SDK_INT >= TIRAMISU) {
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        GrantPermissionRule.grant()
    }

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    @Before
    fun setUp() = runTest {
        notificationManager.createNotificationChannel(
            AnimeNotificationChannelFactory(testCoroutineContextProvider()).create()
        )
        clearScreen()
    }

    @After
    fun tearDown() {
        clearScreen()
    }

    @Test
    fun aBurstOfPostsGetsOneIdEachThoughTheServiceAppliesThemLater() = runTest {
        //Given
        val manager = createManager()

        //When
        repeat(BURST_SIZE) { number: Int ->
            manager.makeNewEpisodeNotification("Burst $number", EPISODE, null)
        }

        //Then
        val singles = awaitSingles { it.size == BURST_SIZE }
        assertEquals(
            (FIRST_SINGLE_ID until FIRST_SINGLE_ID + BURST_SIZE).toList(),
            singles.map { it.id }.sorted()
        )
        assertEquals(
            List(BURST_SIZE) { "Burst $it" }.toSet(),
            singles.map { titleOf(it) }.toSet()
        )
    }

    @Test
    fun aNotificationLeftByAnEarlierProcessIsKept() = runTest {
        //Given
        createManager().makeNewEpisodeNotification("Earlier process", EPISODE, null)
        awaitSingles { it.size == 1 }

        //When
        createManager().makeNewEpisodeNotification("Later process", EPISODE, null)

        //Then
        val singles = awaitSingles { it.size == 2 }
        assertEquals("Earlier process", titleOf(singles.single { it.id == FIRST_SINGLE_ID }))
        assertEquals("Later process", titleOf(singles.single { it.id == FIRST_SINGLE_ID + 1 }))
    }

    @Test
    fun withEveryIdOnScreenTheOneShownLongestIsReplacedAndNoneIsAdded() = runTest {
        //Given
        val postingOrder = (FIRST_SINGLE_ID..LAST_SINGLE_ID).shuffled(Random(SHUFFLE_SEED))
        postingOrder.forEach { id: Int -> postElsewhere(id = id, title = "Earlier $id") }
        SystemClock.sleep(RATE_LIMIT_COOLDOWN_MILLIS)
        val manager = createManager()

        //When
        manager.makeNewEpisodeNotification("Newest", EPISODE, null)

        //Then
        val singles = awaitSingles { shown -> shown.any { titleOf(it) == "Newest" } }
        assertNotEquals(FIRST_SINGLE_ID, postingOrder.first())
        assertEquals(SINGLE_ID_COUNT, singles.size)
        assertEquals("Newest", titleOf(singles.single { it.id == postingOrder.first() }))
    }

    @Test
    fun aDismissedNotificationFreesItsIdForTheNextPost() = runTest {
        //Given
        for (id in FIRST_SINGLE_ID..LAST_SINGLE_ID) {
            postElsewhere(id = id, title = "Earlier $id")
        }
        notificationManager.cancel(FIRST_SINGLE_ID + 2)
        awaitSingles { it.size == SINGLE_ID_COUNT - 1 }
        val manager = createManager()

        //When
        manager.makeNewEpisodeNotification("Into the gap", EPISODE, null)

        //Then
        val singles = awaitSingles { it.size == SINGLE_ID_COUNT }
        assertEquals("Into the gap", titleOf(singles.single { it.id == FIRST_SINGLE_ID + 2 }))
        assertEquals(
            "Earlier $FIRST_SINGLE_ID",
            titleOf(singles.single { it.id == FIRST_SINGLE_ID })
        )
    }

    @Test
    fun theSummarySitsBesideTheSinglesAndTakesNoRingId() = runTest {
        //Given
        val manager = createManager()

        //When
        manager.makeNewEpisodeNotification("With a summary", EPISODE, null)

        //Then
        val shown = awaitShown { all ->
            all.any { it.tag == null && it.id == SUMMARY_ID } && singlesOf(all).size == 1
        }
        assertEquals(FIRST_SINGLE_ID, singlesOf(shown).single().id)
    }

    private fun TestScope.createManager(): AnimeNotificationManager = AnimeNotificationManagerImpl(
        appContext = context,
        animeNotificationIntentProvider = AnimeNotificationIntentProviderFake,
        coroutineContextProvider = testCoroutineContextProvider(),
        // No test passes an image url, so the loader never reaches the network.
        posterLoader = PosterLoader(platformContext = context)
    )

    private fun TestScope.testCoroutineContextProvider() =
        CoroutineContextProviderFake(ioDispatcher = StandardTestDispatcher(testScheduler))

    /** Posts the way an earlier process would have, then waits until the service shows it. */
    private fun postElsewhere(id: Int, title: String) {
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        notificationManager.notify(id, notification)
        awaitShown { shown -> shown.any { it.id == id && titleOf(it) == title } }
        // The service stamps a post with the wall clock in milliseconds. Two in the same one
        // would tie, and these tests rely on each being newer than the one before.
        SystemClock.sleep(2)
    }

    private fun clearScreen() {
        notificationManager.cancelAll()
        awaitShown { it.isEmpty() }
    }

    private fun awaitSingles(
        condition: (List<StatusBarNotification>) -> Boolean
    ): List<StatusBarNotification> = singlesOf(awaitShown { condition(singlesOf(it)) })

    private fun awaitShown(
        condition: (List<StatusBarNotification>) -> Boolean
    ): List<StatusBarNotification> {
        repeat(POLL_ATTEMPTS) {
            val shown = notificationManager.activeNotifications.toList()
            if (condition(shown)) return shown
            SystemClock.sleep(POLL_STEP_MILLIS)
        }
        fail(
            "The notifications on screen never matched. Last seen: " +
                notificationManager.activeNotifications.map { "${it.tag}/${it.id}" }
        )
    }

    private fun singlesOf(shown: List<StatusBarNotification>): List<StatusBarNotification> =
        shown.filter { it.tag == null && it.id in FIRST_SINGLE_ID..LAST_SINGLE_ID }

    private fun titleOf(notification: StatusBarNotification): String? =
        notification.notification.extras.getString(Notification.EXTRA_TITLE)
}
