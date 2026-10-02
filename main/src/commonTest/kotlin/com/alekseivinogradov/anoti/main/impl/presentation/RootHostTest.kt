package com.alekseivinogradov.anoti.main.impl.presentation

import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.model.SectionDomain
import com.alekseivinogradov.anoti.main.impl.di.createDiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.navigation.NavRootChild
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionStatus
import com.alekseivinogradov.anoti.main.impl.presentation.permission.fake.NotificationPermissionRequestsFake
import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

// One function per case under test, plus the helpers those cases share.
@Suppress("TooManyFunctions")
@OptIn(ExperimentalCoroutinesApi::class)
class RootHostTest {

    private val scheduler = TestCoroutineScheduler()

    private lateinit var dependencies: DiRootDependenciesFake

    private val requests = NotificationPermissionRequestsFake()

    private val lifecycles = mutableListOf<LifecycleRegistry>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher(scheduler))
        // The fake hands its dispatcher to the catalog client on creation, so it comes after.
        dependencies = DiRootDependenciesFake()
    }

    @AfterTest
    fun tearDown() {
        lifecycles.filterNot { it.state == Lifecycle.State.DESTROYED }.forEach { it.destroy() }
        Dispatchers.resetMain()
    }

    @Test
    fun opensOnTheAnimeListWhenNothingAsksForAnotherScreen() {
        //Given
        val openingTarget: NavRootConfig? = null

        //When
        val root = createRoot(openingTarget = openingTarget)

        //Then
        assertEquals(NavRootConfig.AnimeList, root.activeScreen)
        assertEquals(listOf(false), root.discardRequests)
    }

    @Test
    fun opensOnTheOpeningTargetAndDropsSavedStateNamingAnotherScreen() {
        //Given
        val savedState = savedStateOn(NavRootConfig.AnimeList)

        //When
        val root = createRoot(openingTarget = NavRootConfig.AnimeFavorites, savedState = savedState)

        //Then
        assertEquals(NavRootConfig.AnimeFavorites, root.activeScreen)
        assertEquals(listOf(true), root.discardRequests)
    }

    @Test
    fun restoresTheSavedScreenWhenNothingAsksForAnother() {
        //Given
        val savedState = savedStateOn(NavRootConfig.AnimeFavorites)

        //When
        val root = createRoot(openingTarget = null, savedState = savedState)

        //Then
        assertEquals(NavRootConfig.AnimeFavorites, root.activeScreen)
        assertEquals(listOf(false), root.discardRequests)
    }

    @Test
    fun theBarOpensOnTheTabOfAScreenItWasAskedToOpen() {
        //Given
        val openingTarget = NavRootConfig.AnimeFavorites

        //When
        val root = createRoot(openingTarget = openingTarget)
        scheduler.advanceUntilIdle()

        //Then
        assertEquals(SectionDomain.FAVORITES, root.selectedSection)
    }

    @Test
    fun theBarOpensOnTheTabOfARestoredScreen() {
        //Given
        val savedState = savedStateOn(NavRootConfig.AnimeFavorites)

        //When
        val root = createRoot(savedState = savedState)
        scheduler.advanceUntilIdle()

        //Then
        assertEquals(SectionDomain.FAVORITES, root.selectedSection)
    }

    @Test
    fun theBarOpensOnTheListTabWhenNothingAsksForAnotherScreen() {
        //Given
        val openingTarget: NavRootConfig? = null

        //When
        val root = createRoot(openingTarget = openingTarget)
        scheduler.advanceUntilIdle()

        //Then
        assertEquals(SectionDomain.MAIN, root.selectedSection)
    }

    @Test
    fun theBarFollowsANavigationWithNoViewBound() {
        //Given
        val root = createRoot()

        //When
        root.host.dependencies.rootComponent.navigateTo(NavRootConfig.AnimeFavorites)

        //Then
        assertEquals(SectionDomain.FAVORITES, root.selectedSection)
    }

    @Test
    fun theBadgeFollowsTheDatabaseWithNoViewBound() {
        //Given
        val root = createRoot()
        root.lifecycle.resume()

        //When
        dependencies.animeDatabaseStores.first().emit(
            listOf(savedAnime(id = 1, hasNewEpisode = true))
        )
        scheduler.advanceUntilIdle()

        //Then
        assertEquals(1, root.host.dependencies.mainStore.state.favoritesBadgeNumber)
    }

    @Test
    fun navigatingBuildsTheOtherScreen() {
        //Given
        val root = createRoot()

        //When
        root.host.dependencies.rootComponent.navigateTo(NavRootConfig.AnimeFavorites)

        //Then
        assertIs<NavRootChild.Favorites>(
            root.host.dependencies.rootComponent.childStack.value.active.instance
        )
    }

    @Test
    fun theBarTakesItsDatabaseStoreBeforeTheFirstScreenTakesOne() {
        //Given
        val openingTarget: NavRootConfig? = null

        //When
        val root = createRoot(openingTarget = openingTarget)

        //Then
        assertSame(
            dependencies.animeDatabaseStores.first(),
            root.host.dependencies.animeDatabaseStore
        )
    }

    @Test
    fun closesBothRootStoresWhenTheHostIsDestroyed() {
        //Given
        val root = createRoot()

        //When
        root.lifecycle.destroy()

        //Then
        assertTrue(root.host.dependencies.mainStore.isDisposed)
        assertTrue(root.host.dependencies.animeDatabaseStore.isDisposed)
    }

    @Test
    fun keepsBothRootStoresOpenWhileTheHostLives() {
        //Given
        val root = createRoot()

        //When
        root.host.dependencies.rootComponent.navigateTo(NavRootConfig.AnimeFavorites)

        //Then
        assertFalse(root.host.dependencies.mainStore.isDisposed)
        assertFalse(root.host.dependencies.animeDatabaseStore.isDisposed)
    }

    @Test
    fun asksTheSystemRightAwayAndShowsNoExplanationWhenNoneIsOwed() {
        //Given
        val root = createRoot()

        //When
        root.host.onNotificationPermissionStatus(
            status(canPrompt = true, isExplanationOwed = false)
        )

        //Then
        assertEquals(1, requests.prompts)
        assertFalse(root.host.notificationsRationale.visible.value)
        assertEquals(0, requests.settingsOpenings)
    }

    @Test
    fun explainsItselfAndWaitsBeforeAskingTheSystem() {
        //Given
        val root = createRoot()

        //When
        root.host.onNotificationPermissionStatus(
            status(canPrompt = true, isExplanationOwed = true)
        )

        //Then
        assertTrue(root.host.notificationsRationale.visible.value)
        assertEquals(0, requests.prompts)
        assertEquals(0, requests.settingsOpenings)
    }

    @Test
    fun asksTheSystemOnceTheExplanationIsAccepted() {
        //Given
        val root = createRoot()
        root.host.onNotificationPermissionStatus(
            status(canPrompt = true, isExplanationOwed = true)
        )

        //When
        root.host.notificationsRationale.onApprove()

        //Then
        assertFalse(root.host.notificationsRationale.visible.value)
        assertEquals(1, requests.prompts)
        assertEquals(0, requests.settingsOpenings)
    }

    @Test
    fun opensTheSettingsOnceTheExplanationIsAcceptedWhenTheSystemCannotAsk() {
        //Given
        val root = createRoot()
        root.host.onNotificationPermissionStatus(
            status(canPrompt = false, isExplanationOwed = false)
        )
        val shownBeforeApproval = root.host.notificationsRationale.visible.value

        //When
        root.host.notificationsRationale.onApprove()

        //Then
        assertTrue(shownBeforeApproval)
        assertFalse(root.host.notificationsRationale.visible.value)
        assertEquals(1, requests.settingsOpenings)
        assertEquals(0, requests.prompts)
    }

    @Test
    fun dropsTheExplanationAndAsksNothingWhenItIsRefused() {
        //Given
        val root = createRoot()
        root.host.onNotificationPermissionStatus(
            status(canPrompt = true, isExplanationOwed = true)
        )

        //When
        root.host.notificationsRationale.onDismiss()

        //Then
        assertFalse(root.host.notificationsRationale.visible.value)
        assertEquals(0, requests.prompts)
        assertEquals(0, requests.settingsOpenings)
    }

    @Test
    fun doesNothingWhenNotificationsAreAllowed() {
        //Given
        val root = createRoot()

        //When
        root.host.onNotificationPermissionStatus(
            NotificationPermissionStatus(
                isAllowed = true,
                canPrompt = true,
                isExplanationOwed = false
            )
        )

        //Then
        assertFalse(root.host.notificationsRationale.visible.value)
        assertEquals(0, requests.prompts)
        assertEquals(0, requests.settingsOpenings)
    }

    private fun status(canPrompt: Boolean, isExplanationOwed: Boolean) =
        NotificationPermissionStatus(
            isAllowed = false,
            canPrompt = canPrompt,
            isExplanationOwed = isExplanationOwed
        )

    /** What a host that ended up on [screen] leaves behind for the next one. */
    private fun savedStateOn(screen: NavRootConfig): SerializableContainer {
        val root = createRoot(openingTarget = screen)
        val savedState = root.stateKeeper.save()
        root.lifecycle.destroy()
        return savedState
    }

    private fun createRoot(
        openingTarget: NavRootConfig? = null,
        savedState: SerializableContainer? = null
    ): Root {
        val lifecycle = LifecycleRegistry().also(lifecycles::add)
        val discardRequests = mutableListOf<Boolean>()
        var stateKeeper: StateKeeperDispatcher? = null
        val host = RootHost(
            diRootComponent = createDiRootComponent(parent = dependencies),
            openingTarget = openingTarget,
            createComponentContext = { discardSavedState: Boolean ->
                discardRequests += discardSavedState
                val keeper = StateKeeperDispatcher(savedState.takeUnless { discardSavedState })
                stateKeeper = keeper
                DefaultComponentContext(lifecycle = lifecycle, stateKeeper = keeper)
            },
            notificationPermissionRequests = requests
        )
        // A platform host builds its root while its own lifecycle is being created.
        lifecycle.create()
        return Root(
            host = host,
            lifecycle = lifecycle,
            stateKeeper = checkNotNull(stateKeeper),
            discardRequests = discardRequests
        )
    }

    private class Root(
        val host: RootHost,
        val lifecycle: LifecycleRegistry,
        val stateKeeper: StateKeeperDispatcher,
        val discardRequests: List<Boolean>
    ) {
        val activeScreen: NavRootConfig
            get() = host.dependencies.rootComponent.childStack.value.active.configuration

        val selectedSection: SectionDomain
            get() = host.dependencies.mainStore.state.selectedSection
    }
}
