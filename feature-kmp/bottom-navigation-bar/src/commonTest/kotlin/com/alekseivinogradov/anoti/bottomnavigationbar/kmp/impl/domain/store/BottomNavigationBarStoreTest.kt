package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.domain.store

import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.model.SectionDomain
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val BADGE_NUMBER = 5

@OptIn(ExperimentalCoroutinesApi::class)
class BottomNavigationBarStoreTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val createdStores = mutableListOf<Store<*, *, *>>()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        createdStores.forEach { it.dispose() }
        Dispatchers.resetMain()
    }

    private fun createStore(): BottomNavigationBarStore {
        return BottomNavigationBarStoreFactory(DefaultStoreFactory())
            .create()
            .also(createdStores::add)
    }

    private fun TestScope.collectLabels(
        store: BottomNavigationBarStore
    ): List<BottomNavigationBarStore.Label> {
        val labels = mutableListOf<BottomNavigationBarStore.Label>()
        backgroundScope.launch(testDispatcher) { store.labels.collect(labels::add) }
        return labels
    }

    /** The first element is the state the store replays on subscription, not a change. */
    private fun TestScope.collectStates(
        store: BottomNavigationBarStore
    ): List<BottomNavigationBarStore.State> {
        val states = mutableListOf<BottomNavigationBarStore.State>()
        backgroundScope.launch(testDispatcher) { store.states.collect(states::add) }
        return states
    }

    @Test
    fun aFreshStoreStartsOnTheMainSectionWithAnEmptyBadge() = runTest {
        //Given
        val store = createStore()

        //When
        val state = store.state

        //Then
        assertEquals(SectionDomain.MAIN, state.selectedSection)
        assertEquals(0, state.favoritesBadgeNumber)
    }

    @Test
    fun changingTheSelectedSectionSelectsIt() = runTest {
        //Given
        val store = createStore()

        //When
        store.accept(
            BottomNavigationBarStore.Intent.ChangeSelectedSection(SectionDomain.FAVORITES)
        )

        //Then
        assertEquals(SectionDomain.FAVORITES, store.state.selectedSection)
    }

    @Test
    fun updatingTheBadgeNumberLeavesTheSelectedSectionAlone() = runTest {
        //Given
        val store = createStore()
        store.accept(
            BottomNavigationBarStore.Intent.ChangeSelectedSection(SectionDomain.FAVORITES)
        )

        //When
        store.accept(BottomNavigationBarStore.Intent.UpdateFavoritesBadgeNumber(BADGE_NUMBER))

        //Then
        assertEquals(BADGE_NUMBER, store.state.favoritesBadgeNumber)
        assertEquals(SectionDomain.FAVORITES, store.state.selectedSection)
    }

    @Test
    fun selectingTheSectionThatIsAlreadySelectedEmitsNoNewState() = runTest {
        //Given
        val store = createStore()
        store.accept(
            BottomNavigationBarStore.Intent.ChangeSelectedSection(SectionDomain.FAVORITES)
        )
        val states = collectStates(store)

        //When
        store.accept(
            BottomNavigationBarStore.Intent.ChangeSelectedSection(SectionDomain.FAVORITES)
        )

        //Then
        assertEquals(1, states.size)
        assertEquals(SectionDomain.FAVORITES, store.state.selectedSection)
    }

    @Test
    fun updatingTheBadgeToTheNumberItAlreadyShowsEmitsNoNewState() = runTest {
        //Given
        val store = createStore()
        store.accept(BottomNavigationBarStore.Intent.UpdateFavoritesBadgeNumber(BADGE_NUMBER))
        val states = collectStates(store)

        //When
        store.accept(BottomNavigationBarStore.Intent.UpdateFavoritesBadgeNumber(BADGE_NUMBER))

        //Then
        assertEquals(1, states.size)
        assertEquals(BADGE_NUMBER, store.state.favoritesBadgeNumber)
    }

    @Test
    fun aDifferentSectionStillEmitsANewState() = runTest {
        //Given
        val store = createStore()
        val states = collectStates(store)

        //When
        store.accept(
            BottomNavigationBarStore.Intent.ChangeSelectedSection(SectionDomain.FAVORITES)
        )

        //Then
        assertEquals(2, states.size)
        assertEquals(SectionDomain.FAVORITES, states.last().selectedSection)
    }

    @Test
    fun tappingTheMainTabAsksToNavigateWithoutSelectingItItself() = runTest {
        //Given
        val store = createStore()
        val labels = collectLabels(store)
        store.accept(
            BottomNavigationBarStore.Intent.ChangeSelectedSection(SectionDomain.FAVORITES)
        )

        //When
        store.accept(BottomNavigationBarStore.Intent.MainSectionClick)

        //Then
        assertEquals(listOf(BottomNavigationBarStore.Label.NavigateToMain), labels)
        assertEquals(SectionDomain.FAVORITES, store.state.selectedSection)
    }

    @Test
    fun tappingTheFavoritesTabAsksToNavigateWithoutSelectingItItself() = runTest {
        //Given
        val store = createStore()
        val labels = collectLabels(store)

        //When
        store.accept(BottomNavigationBarStore.Intent.FavoritesSectionClick)

        //Then
        assertEquals(listOf(BottomNavigationBarStore.Label.NavigateToFavorites), labels)
        assertEquals(SectionDomain.MAIN, store.state.selectedSection)
    }

    @Test
    fun tappingTheTabThatIsAlreadySelectedStillAsksToNavigate() = runTest {
        //Given
        val store = createStore()
        val labels = collectLabels(store)

        //When
        store.accept(BottomNavigationBarStore.Intent.MainSectionClick)

        //Then
        assertEquals(listOf(BottomNavigationBarStore.Label.NavigateToMain), labels)
    }

    @Test
    fun repeatedTapsEachAskToNavigateOnceMore() = runTest {
        //Given
        val store = createStore()
        val labels = collectLabels(store)

        //When
        store.accept(BottomNavigationBarStore.Intent.FavoritesSectionClick)
        store.accept(BottomNavigationBarStore.Intent.FavoritesSectionClick)

        //Then
        assertEquals(
            listOf(
                BottomNavigationBarStore.Label.NavigateToFavorites,
                BottomNavigationBarStore.Label.NavigateToFavorites
            ),
            labels
        )
    }
}
