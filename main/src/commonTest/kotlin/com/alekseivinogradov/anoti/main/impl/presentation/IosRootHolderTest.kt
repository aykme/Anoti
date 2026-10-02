package com.alekseivinogradov.anoti.main.impl.presentation

import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.alekseivinogradov.anoti.main.impl.di.createDiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionStatus
import com.alekseivinogradov.anoti.main.impl.presentation.permission.fake.NotificationPermissionRequestsFake
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

private const val APP_VERSION = "1.1 (10)"
private const val PROBE_KEY = "probe"

// One function per case under test, plus the helpers those cases share.
@Suppress("TooManyFunctions")
@OptIn(ExperimentalCoroutinesApi::class)
class IosRootHolderTest {

    private val scheduler = TestCoroutineScheduler()

    private lateinit var dependencies: DiRootDependenciesFake

    private val requests = NotificationPermissionRequestsFake()

    private val appLifecycles = mutableListOf<LifecycleRegistry>()

    private var permissionReads = 0

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher(scheduler))
        // The fake hands its dispatcher to the catalog client on creation, so it comes after.
        dependencies = DiRootDependenciesFake()
    }

    @AfterTest
    fun tearDown() {
        appLifecycles.filterNot { it.state == Lifecycle.State.DESTROYED }.forEach { it.destroy() }
        Dispatchers.resetMain()
    }

    @Test
    fun buildsNothingAndSavesNothingBeforeAScreenAsks() {
        //Given
        val holder = createHolder()

        //When
        val saved = holder.saveState()

        //Then
        assertNull(saved)
        assertTrue(dependencies.animeDatabaseStores.isEmpty())
    }

    @Test
    fun aLaterScreenGetsTheSameRootAndItsStringIsIgnored() {
        //Given
        val holder = createHolder()
        val first = holder.rootFor(restoredState = null)

        //When
        val second = holder.rootFor(restoredState = savedOn(NavRootConfig.AnimeFavorites))

        //Then
        assertSame(first, second)
        assertEquals(NavRootConfig.AnimeList, second.activeScreen)
    }

    @Test
    fun restoresTheScreenTheStringNames() {
        //Given
        val saved = savedOn(NavRootConfig.AnimeFavorites)

        //When
        val root = createHolder().rootFor(restoredState = saved)

        //Then
        assertEquals(NavRootConfig.AnimeFavorites, root.activeScreen)
    }

    @Test
    fun anEmptyStringGivesAFreshRoot() {
        //Given
        val holder = createHolder()

        //When
        val root = holder.rootFor(restoredState = "")

        //Then
        assertEquals(NavRootConfig.AnimeList, root.activeScreen)
    }

    @Test
    fun aStringThatDoesNotParseGivesAFreshRoot() {
        //Given
        val holder = createHolder()

        //When
        val root = holder.rootFor(restoredState = "{")

        //Then
        assertEquals(NavRootConfig.AnimeList, root.activeScreen)
    }

    @Test
    fun aStateFromAnotherBuildIsDropped() {
        //Given
        val saved = savedOn(NavRootConfig.AnimeFavorites)

        //When
        val root = createHolder(appVersion = "1.2 (11)").rootFor(restoredState = saved)

        //Then
        assertEquals(NavRootConfig.AnimeList, root.activeScreen)
    }

    @Test
    fun aStateTheScreensRejectGivesAFreshRootAndClosesTheAttempt() {
        //Given
        val broken = stringOf(
            StateKeeperDispatcher().apply {
                register(key = CHILD_STACK_KEY, strategy = String.serializer()) { "not a stack" }
            }.save()
        )

        //When
        val root = createHolder().rootFor(restoredState = broken)

        //Then
        assertEquals(NavRootConfig.AnimeList, root.activeScreen)
        assertTrue(dependencies.animeDatabaseStores.first().isDisposed, "the attempt kept running")
        assertFalse(root.dependencies.animeDatabaseStore.isDisposed)
    }

    @Test
    fun aRejectedStateIsClosedEvenBeforeTheAppHasStarted() {
        //Given
        // iOS sets the app's first state a main-queue turn after the lifecycle is made.
        val holder = createHolder(isAppStarted = false)
        val broken = stringOf(
            StateKeeperDispatcher().apply {
                register(key = CHILD_STACK_KEY, strategy = String.serializer()) { "not a stack" }
            }.save()
        )

        //When
        holder.rootFor(restoredState = broken)

        //Then
        assertTrue(dependencies.animeDatabaseStores.first().isDisposed, "the attempt kept running")
    }

    @Test
    fun aTapBeforeTheRootOpensItOnItsScreen() {
        //Given
        val holder = createHolder()

        //When
        holder.openFromNotification(NavRootConfig.AnimeFavorites)
        val root = holder.rootFor(restoredState = null)

        //Then
        assertEquals(NavRootConfig.AnimeFavorites, root.activeScreen)
    }

    @Test
    fun aTapBeforeTheRootRestoresTheStateThenOpensItsScreen() {
        //Given
        val before = createHolder()
        before.rootFor(restoredState = null)
        before.attach(before.newSaveableStateRegistry().withProbe("kept"))
        val saved = checkNotNull(before.saveState())
        val after = createHolder()

        //When
        after.openFromNotification(NavRootConfig.AnimeFavorites)
        val root = after.rootFor(restoredState = saved)

        //Then
        assertEquals(NavRootConfig.AnimeFavorites, root.activeScreen)
        assertEquals("kept", after.newSaveableStateRegistry().consumeRestored(PROBE_KEY))
    }

    @Test
    fun aTapOnTheLiveRootNavigatesIt() {
        //Given
        val holder = createHolder()
        val root = holder.rootFor(restoredState = null)

        //When
        holder.openFromNotification(NavRootConfig.AnimeFavorites)

        //Then
        assertEquals(NavRootConfig.AnimeFavorites, root.activeScreen)
        assertSame(root, holder.rootFor(restoredState = null))
    }

    @Test
    fun savesWhatTheLiveCompositionHolds() {
        //Given
        val holder = createHolder()
        holder.rootFor(restoredState = null)
        holder.attach(holder.newSaveableStateRegistry().withProbe("live"))

        //When
        val saved = holder.saveState()

        //Then
        assertEquals("live", restoredProbe(saved))
    }

    @Test
    fun savesWhatTheLastCompositionHeldOnceItEnded() {
        //Given
        val holder = createHolder()
        holder.rootFor(restoredState = null)
        val registry = holder.newSaveableStateRegistry().withProbe("ended")
        holder.attach(registry)

        //When
        holder.detach(registry)
        val saved = holder.saveState()

        //Then
        assertEquals("ended", restoredProbe(saved))
    }

    @Test
    fun aRegistryNoLongerLiveChangesNothing() {
        //Given
        val holder = createHolder()
        holder.rootFor(restoredState = null)
        val old = holder.newSaveableStateRegistry().withProbe("old")
        holder.attach(old)
        holder.attach(holder.newSaveableStateRegistry().withProbe("new"))

        //When
        holder.detach(old)

        //Then
        assertEquals("new", restoredProbe(holder.saveState()))
    }

    @Test
    fun aNewCompositionStartsFromTheLiveOne() {
        //Given
        val holder = createHolder()
        holder.rootFor(restoredState = null)
        holder.attach(holder.newSaveableStateRegistry().withProbe("live"))

        //When
        val next = holder.newSaveableStateRegistry()

        //Then
        assertEquals("live", next.consumeRestored(PROBE_KEY))
    }

    @Test
    fun checksThePermissionOncePerProcess() {
        //Given
        val holder = createHolder()

        //When
        holder.rootFor(restoredState = null)
        holder.rootFor(restoredState = null)
        scheduler.advanceUntilIdle()

        //Then
        assertEquals(1, permissionReads)
        assertEquals(1, requests.prompts)
    }

    @Test
    fun theRootEndsWithTheApp() {
        //Given
        val holder = createHolder()
        val root = holder.rootFor(restoredState = null)

        //When
        appLifecycles.last().destroy()

        //Then
        assertTrue(root.dependencies.mainStore.isDisposed)
        assertTrue(root.dependencies.animeDatabaseStore.isDisposed)
    }

    private fun createHolder(
        appVersion: String = APP_VERSION,
        isAppStarted: Boolean = true
    ): IosRootHolder {
        val appLifecycle = LifecycleRegistry().also(appLifecycles::add)
        if (isAppStarted) {
            appLifecycle.resume()
        }
        return IosRootHolder(
            createDiRootComponent = { createDiRootComponent(parent = dependencies) },
            appLifecycle = appLifecycle,
            appVersion = appVersion,
            notificationPermissionRequests = requests,
            readNotificationPermissionStatus = {
                permissionReads++
                ASKABLE
            }
        )
    }

    /** What a holder whose root ended up on [screen] leaves for the next process. */
    private fun savedOn(screen: NavRootConfig): String {
        val holder = createHolder()
        holder.rootFor(restoredState = null).openFromNotification(screen)
        return checkNotNull(holder.saveState())
    }

    private fun stringOf(container: SerializableContainer): String = JsonObject(
        mapOf(
            "appVersion" to JsonPrimitive(APP_VERSION),
            "stateKeeper" to Json.encodeToJsonElement(
                SerializableContainer.serializer(),
                container
            ),
            "saveable" to JsonObject(emptyMap())
        )
    ).toString()

    private fun restoredProbe(saved: String?): Any? {
        val holder = createHolder()
        holder.rootFor(restoredState = saved)
        return holder.newSaveableStateRegistry().consumeRestored(PROBE_KEY)
    }

    private fun SaveableStateRegistry.withProbe(value: String) = apply {
        registerProvider(PROBE_KEY) { value }
    }

    private val RootHost.activeScreen: NavRootConfig
        get() = dependencies.rootComponent.childStack.value.active.configuration

    private companion object {
        // The key Decompose saves a child stack under when none is given.
        const val CHILD_STACK_KEY = "DefaultChildStack"

        val ASKABLE = NotificationPermissionStatus(
            isAllowed = false,
            canPrompt = true,
            isExplanationOwed = false
        )
    }
}
