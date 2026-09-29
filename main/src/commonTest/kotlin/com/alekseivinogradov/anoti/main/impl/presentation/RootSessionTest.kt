package com.alekseivinogradov.anoti.main.impl.presentation

import com.alekseivinogradov.anoti.main.impl.di.createDiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.compose.RememberedRoot
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionStatus
import com.alekseivinogradov.anoti.main.impl.presentation.permission.fake.NotificationPermissionRequestsFake
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame

// One function per case under test, plus the helpers those cases share.
@Suppress("TooManyFunctions")
@OptIn(ExperimentalCoroutinesApi::class)
class RootSessionTest {

    private val scheduler = TestCoroutineScheduler()

    private lateinit var dependencies: DiRootDependenciesFake

    private val requests = NotificationPermissionRequestsFake()

    private val lifecycles = mutableListOf<LifecycleRegistry>()

    private var permissionStatus = CompletableDeferred(ASKABLE)

    private lateinit var session: RootSession

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher(scheduler))
        // The fake hands its dispatcher to the catalog client on creation, so it comes after.
        dependencies = DiRootDependenciesFake()
        session = createSession()
    }

    @AfterTest
    fun tearDown() {
        lifecycles.forEach { it.destroy() }
        Dispatchers.resetMain()
    }

    @Test
    fun theFirstRootOpensOnTheAnimeListAndBecomesTheCurrentOne() {
        //Given
        val generation = session.generation

        //When
        val root = session.createRoot()

        //Then
        assertEquals(NavRootConfig.AnimeList, root.activeScreen)
        assertSame(root, session.currentRoot)
        assertEquals(0, generation)
    }

    @Test
    fun endingARootDestroysItsLifecycleAndLeavesNoCurrentRoot() {
        //Given
        val root = session.createRoot()

        //When
        session.endRoot(root)

        //Then
        assertEquals(Lifecycle.State.DESTROYED, root.lifecycle.lifecycle.state)
        assertNull(session.currentRoot)
    }

    @Test
    fun endingARootThatIsNoLongerCurrentLeavesTheCurrentOneAlone() {
        //Given
        val first = session.createRoot()
        val second = session.createRoot()

        //When
        session.endRoot(first)

        //Then
        assertSame(second, session.currentRoot)
        assertEquals(Lifecycle.State.CREATED, second.lifecycle.lifecycle.state)
    }

    @Test
    fun aRootAbandonedBeforeItWasRememberedIsEndedToo() {
        //Given
        val remembered = RememberedRoot(session)

        //When
        remembered.onAbandoned()

        //Then
        assertEquals(Lifecycle.State.DESTROYED, remembered.root.lifecycle.lifecycle.state)
        assertNull(session.currentRoot)
    }

    @Test
    fun aForgottenRootIsEnded() {
        //Given
        val remembered = RememberedRoot(session)
        remembered.onRemembered()

        //When
        remembered.onForgotten()

        //Then
        assertEquals(Lifecycle.State.DESTROYED, remembered.root.lifecycle.lifecycle.state)
    }

    @Test
    fun aTapBeforeAnyRootOpensTheFirstRootOnItsScreenWithoutARebuild() {
        //Given
        session.openFromNotification(NavRootConfig.AnimeFavorites)

        //When
        val first = session.createRoot()

        //Then
        assertEquals(NavRootConfig.AnimeFavorites, first.activeScreen)
        assertEquals(0, session.generation)
    }

    @Test
    fun theRootAfterTheOneATapOpenedNoLongerSeesTheTap() {
        //Given
        session.openFromNotification(NavRootConfig.AnimeFavorites)
        session.endRoot(session.createRoot())

        //When
        val next = session.createRoot()

        //Then
        assertEquals(NavRootConfig.AnimeList, next.activeScreen)
    }

    @Test
    fun aTapWithALiveRootAsksForANewRootThatOpensOnItsScreen() {
        //Given
        val live = session.createRoot()

        //When
        session.openFromNotification(NavRootConfig.AnimeFavorites)
        val next = session.createRoot()
        session.endRoot(live)

        //Then
        assertEquals(1, session.generation)
        assertEquals(NavRootConfig.AnimeFavorites, next.activeScreen)
        assertSame(next, session.currentRoot)
    }

    @Test
    fun aTapOnTheScreenAlreadyOpenStillRebuilds() {
        //Given
        session.openFromNotification(NavRootConfig.AnimeFavorites)
        session.createRoot()

        //When
        session.openFromNotification(NavRootConfig.AnimeFavorites)

        //Then
        assertEquals(1, session.generation)
    }

    @Test
    fun thePermissionCheckActsOnTheStatusOfItsRoot() = runTest {
        //Given
        val root = session.createRoot()

        //When
        session.checkNotificationPermission(root)

        //Then
        assertEquals(1, requests.prompts)
    }

    @Test
    fun aStatusThatArrivesAfterItsRootEndedIsDropped() = runTest {
        //Given
        permissionStatus = CompletableDeferred()
        val root = session.createRoot()
        launch { session.checkNotificationPermission(root) }
        advanceUntilIdle()

        //When
        session.endRoot(root)
        session.createRoot()
        permissionStatus.complete(EXPLANATION_OWED)
        advanceUntilIdle()

        //Then
        assertFalse(root.host.notificationsRationale.visible.value)
    }

    @Test
    fun aStatusForARootReplacedWhileItWasReadIsDropped() = runTest {
        //Given
        permissionStatus = CompletableDeferred()
        val first = session.createRoot()
        launch { session.checkNotificationPermission(first) }
        advanceUntilIdle()
        val second = session.createRoot()

        //When
        permissionStatus.complete(ASKABLE)
        advanceUntilIdle()

        //Then
        assertEquals(0, requests.prompts)
        assertFalse(second.host.notificationsRationale.visible.value)
    }

    private fun createSession() = RootSession(
        createDiRootComponent = { createDiRootComponent(parent = dependencies) },
        createLifecycle = ::createLifecycle,
        notificationPermissionRequests = requests,
        readNotificationPermissionStatus = { permissionStatus.await() }
    )

    // Created at once, as iOS's own lifecycle settles right after the root is built. Ended the
    // way iOS ends its own, from any state.
    private fun createLifecycle(): RootLifecycle {
        val lifecycle = LifecycleRegistry().also(lifecycles::add)
        lifecycle.create()
        return RootLifecycle(lifecycle = lifecycle, end = lifecycle::destroy)
    }

    private val SessionRoot.activeScreen: NavRootConfig
        get() = host.dependencies.rootComponent.childStack.value.active.configuration

    private companion object {
        val ASKABLE = NotificationPermissionStatus(
            isAllowed = false,
            canPrompt = true,
            isExplanationOwed = false
        )
        val EXPLANATION_OWED = NotificationPermissionStatus(
            isAllowed = false,
            canPrompt = true,
            isExplanationOwed = true
        )
    }
}
