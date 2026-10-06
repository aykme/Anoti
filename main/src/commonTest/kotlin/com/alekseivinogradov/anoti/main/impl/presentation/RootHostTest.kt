package com.alekseivinogradov.anoti.main.impl.presentation

import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.model.SectionDomain
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.main.impl.di.createDiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.navigation.NavRootChild
import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionSession
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

    // One process for the whole test, unless a case builds a root in a new one.
    private val session = NotificationPermissionSession()

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
    fun theBarFollowsANavigationWithNoCompositionShown() {
        //Given
        val root = createRoot()

        //When
        root.host.dependencies.rootComponent.navigateTo(NavRootConfig.AnimeFavorites)

        //Then
        assertEquals(SectionDomain.FAVORITES, root.selectedSection)
    }

    @Test
    fun theBadgeFollowsTheDatabaseWithNoCompositionShown() {
        //Given
        val root = createRoot()
        root.lifecycle.resume()

        //When
        dependencies.animeDatabaseStores.first().emit(
            listOf(savedAnime(id = 1, hasNewEpisode = true))
        )
        scheduler.advanceUntilIdle()

        //Then
        assertEquals(1, root.host.dependencies.barController.state.value.favoritesBadgeNumber)
    }

    @Test
    fun aTapOnTheBarsOtherTabNavigatesTheRoot() {
        //Given
        val root = createRoot()
        root.lifecycle.resume()
        scheduler.advanceUntilIdle()

        //When
        root.host.dependencies.barController.accept(
            BottomNavigationBarStore.Intent.FavoritesSectionClick
        )
        scheduler.advanceUntilIdle()

        //Then
        assertEquals(NavRootConfig.AnimeFavorites, root.activeScreen)
        assertEquals(SectionDomain.FAVORITES, root.selectedSection)
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
    fun aTapOpensItsScreenInTheLiveRoot() {
        //Given
        val root = createRoot()

        //When
        root.host.openFromNotification(NavRootConfig.AnimeFavorites)

        //Then
        assertEquals(NavRootConfig.AnimeFavorites, root.activeScreen)
        assertEquals(SectionDomain.FAVORITES, root.selectedSection)
        assertFalse(dependencies.barStores.single().isDisposed)
    }

    @Test
    fun aTapOnTheScreenAlreadyShownKeepsIt() {
        //Given
        val root = createRoot(openingTarget = NavRootConfig.AnimeFavorites)
        val shown = root.host.dependencies.rootComponent.childStack.value.active.instance

        //When
        root.host.openFromNotification(NavRootConfig.AnimeFavorites)

        //Then
        assertSame(shown, root.host.dependencies.rootComponent.childStack.value.active.instance)
    }

    @Test
    fun closesBothRootStoresWhenTheHostIsDestroyed() {
        //Given
        val root = createRoot()

        //When
        root.lifecycle.destroy()

        //Then
        assertTrue(dependencies.barStores.single().isDisposed)
        assertTrue(dependencies.animeDatabaseStores.first().isDisposed)
    }

    @Test
    fun keepsBothRootStoresOpenWhileTheHostLives() {
        //Given
        val root = createRoot()

        //When
        root.host.dependencies.rootComponent.navigateTo(NavRootConfig.AnimeFavorites)

        //Then
        assertFalse(dependencies.barStores.single().isDisposed)
        assertFalse(dependencies.animeDatabaseStores.first().isDisposed)
    }

    @Test
    fun asksTheSystemRightAwayAndShowsNoExplanationWhenNoneIsOwed() {
        //Given
        val root = createRoot()

        //When
        root.host.onNotificationPermissionStatus(
            status = status(canPrompt = true, isExplanationOwed = false),
            isRebuilt = false
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
            status = status(canPrompt = true, isExplanationOwed = true),
            isRebuilt = false
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
            status = status(canPrompt = true, isExplanationOwed = true),
            isRebuilt = false
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
            status = status(canPrompt = false, isExplanationOwed = false),
            isRebuilt = false
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
            status = status(canPrompt = true, isExplanationOwed = true),
            isRebuilt = false
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
        root.host.onNotificationPermissionStatus(status = ALLOWED, isRebuilt = false)

        //Then
        assertFalse(root.host.notificationsRationale.visible.value)
        assertEquals(0, requests.prompts)
        assertEquals(0, requests.settingsOpenings)
    }

    @Test
    fun aRootRebuiltInTheSameProcessAsksNothingAgain() {
        //Given
        val first = createRoot()
        first.host.onNotificationPermissionStatus(
            status = status(canPrompt = true, isExplanationOwed = false),
            isRebuilt = false
        )
        first.lifecycle.destroy()
        val rebuilt = createRoot()

        //When
        rebuilt.host.onNotificationPermissionStatus(
            status = status(canPrompt = true, isExplanationOwed = false),
            isRebuilt = true
        )

        //Then
        assertEquals(1, requests.prompts)
        assertFalse(rebuilt.host.notificationsRationale.visible.value)
    }

    @Test
    fun aRootRebuiltInTheSameProcessKeepsTheExplanationOnScreen() {
        //Given
        val first = createRoot()
        first.host.onNotificationPermissionStatus(
            status = status(canPrompt = false, isExplanationOwed = false),
            isRebuilt = false
        )
        first.lifecycle.destroy()
        val rebuilt = createRoot()
        rebuilt.host.onNotificationPermissionStatus(
            status = status(canPrompt = false, isExplanationOwed = false),
            isRebuilt = true
        )
        val shownAfterRebuild = rebuilt.host.notificationsRationale.visible.value

        //When
        rebuilt.host.notificationsRationale.onApprove()

        //Then
        assertTrue(shownAfterRebuild)
        assertEquals(1, requests.settingsOpenings)
        assertEquals(0, requests.prompts)
    }

    @Test
    fun aRootRebuiltInTheSameProcessDropsTheExplanationOnceNotificationsAreAllowed() {
        //Given
        val first = createRoot()
        first.host.onNotificationPermissionStatus(
            status = status(canPrompt = true, isExplanationOwed = true),
            isRebuilt = false
        )
        first.lifecycle.destroy()
        val rebuilt = createRoot()

        //When
        rebuilt.host.onNotificationPermissionStatus(status = ALLOWED, isRebuilt = true)

        //Then
        assertFalse(rebuilt.host.notificationsRationale.visible.value)
        assertEquals(0, requests.prompts)
    }

    @Test
    fun aRootRebuiltInTheSameProcessKeepsARefusedExplanationClosed() {
        //Given
        val first = createRoot()
        first.host.onNotificationPermissionStatus(
            status = status(canPrompt = true, isExplanationOwed = true),
            isRebuilt = false
        )
        first.host.notificationsRationale.onDismiss()
        first.lifecycle.destroy()
        val rebuilt = createRoot()

        //When
        rebuilt.host.onNotificationPermissionStatus(
            status = status(canPrompt = true, isExplanationOwed = true),
            isRebuilt = true
        )

        //Then
        assertFalse(rebuilt.host.notificationsRationale.visible.value)
        assertEquals(0, requests.prompts)
    }

    @Test
    fun aRootRebuiltInANewProcessExplainsItselfAgain() {
        //Given
        val first = createRoot()
        first.host.onNotificationPermissionStatus(
            status = status(canPrompt = true, isExplanationOwed = true),
            isRebuilt = false
        )
        first.host.notificationsRationale.onDismiss()
        first.lifecycle.destroy()
        val rebuilt = createRoot(session = NotificationPermissionSession())

        //When
        rebuilt.host.onNotificationPermissionStatus(
            status = status(canPrompt = true, isExplanationOwed = true),
            isRebuilt = true
        )

        //Then
        assertTrue(rebuilt.host.notificationsRationale.visible.value)
    }

    @Test
    fun aFreshStartInTheSameProcessExplainsItselfAgain() {
        //Given
        val first = createRoot()
        first.host.onNotificationPermissionStatus(
            status = status(canPrompt = true, isExplanationOwed = true),
            isRebuilt = false
        )
        first.host.notificationsRationale.onDismiss()
        first.lifecycle.destroy()
        val next = createRoot()

        //When
        next.host.onNotificationPermissionStatus(
            status = status(canPrompt = true, isExplanationOwed = true),
            isRebuilt = false
        )

        //Then
        assertTrue(next.host.notificationsRationale.visible.value)
    }

    @Test
    fun aFreshStartThatAsksTheSystemDropsAnEarlierExplanation() {
        //Given
        val first = createRoot()
        first.host.onNotificationPermissionStatus(
            status = status(canPrompt = true, isExplanationOwed = true),
            isRebuilt = false
        )
        first.lifecycle.destroy()
        val next = createRoot()

        //When
        next.host.onNotificationPermissionStatus(
            status = status(canPrompt = true, isExplanationOwed = false),
            isRebuilt = false
        )

        //Then
        assertFalse(next.host.notificationsRationale.visible.value)
        assertEquals(1, requests.prompts)
    }

    @Test
    fun aFreshStartDropsAnExplanationOnceNotificationsAreAllowed() {
        //Given
        val first = createRoot()
        first.host.onNotificationPermissionStatus(
            status = status(canPrompt = true, isExplanationOwed = true),
            isRebuilt = false
        )
        first.lifecycle.destroy()
        val next = createRoot()

        //When
        next.host.onNotificationPermissionStatus(status = ALLOWED, isRebuilt = false)

        //Then
        assertFalse(next.host.notificationsRationale.visible.value)
        assertEquals(0, requests.prompts)
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
        savedState: SerializableContainer? = null,
        session: NotificationPermissionSession = this.session
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
            notificationPermissionRequests = requests,
            notificationPermissionSession = session
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

    private companion object {
        val ALLOWED = NotificationPermissionStatus(
            isAllowed = true,
            canPrompt = true,
            isExplanationOwed = false
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
            get() = host.dependencies.barController.state.value.selectedSection
    }
}
