package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.fake.AnimeDatabaseStoreFake
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.model.SectionDomain
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.domain.store.BottomNavigationBarStoreFactory
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val FIRST_ID = 11
private const val SECOND_ID = 22

@OptIn(ExperimentalCoroutinesApi::class)
class BottomNavigationBarControllerTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val lifecycle = LifecycleRegistry()

    private val labels = mutableListOf<BottomNavigationBarStore.Label>()

    private lateinit var mainStore: BottomNavigationBarStore

    private lateinit var databaseStore: AnimeDatabaseStore

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mainStore = BottomNavigationBarStoreFactory(DefaultStoreFactory()).create()
        databaseStore = AnimeDatabaseStoreFake()
    }

    @AfterTest
    fun tearDown() {
        if (lifecycle.state != Lifecycle.State.DESTROYED) {
            lifecycle.destroy()
        }
        mainStore.dispose()
        databaseStore.dispose()
        Dispatchers.resetMain()
    }

    private fun startController(): BottomNavigationBarController {
        val controller = BottomNavigationBarController(
            lifecycle = lifecycle,
            mainStore = mainStore,
            animeDatabaseStore = databaseStore,
            onLabel = labels::add
        )
        lifecycle.resume()
        return controller
    }

    private fun dbItem(
        id: AnimeId,
        isNewEpisode: Boolean,
        name: String = "Frieren"
    ) = AnimeDbDomain(
        id = id,
        imageUrl = null,
        name = name,
        episodesAired = 7,
        episodesTotal = 28,
        nextEpisodeAt = null,
        airedOn = null,
        releasedOn = null,
        score = 9.1F,
        releaseStatus = ReleaseStatusDb.ONGOING,
        episodesViewed = 0,
        isNewEpisode = isNewEpisode
    )

    @Test
    fun theStateHoldsWhatTheStoreHoldsAsSoonAsItIsBuilt() = runTest {
        //Given
        mainStore.accept(
            BottomNavigationBarStore.Intent.ChangeSelectedSection(SectionDomain.FAVORITES)
        )

        //When
        val controller = startController()

        //Then
        assertEquals(mainStore.state, controller.state.value)
        assertEquals(SectionDomain.FAVORITES, controller.state.value.selectedSection)
    }

    @Test
    fun anItemWithANewEpisodeReachesTheStateAsABadgeNumber() = runTest {
        //Given
        val controller = startController()

        //When
        databaseStore.accept(
            AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(
                dbItem(id = FIRST_ID, isNewEpisode = true)
            )
        )

        //Then
        assertEquals(1, controller.state.value.favoritesBadgeNumber)
    }

    @Test
    fun clearingTheLastNewEpisodeTakesTheBadgeBackToZero() = runTest {
        //Given
        val controller = startController()
        databaseStore.accept(
            AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(
                dbItem(id = FIRST_ID, isNewEpisode = true)
            )
        )
        assertEquals(1, controller.state.value.favoritesBadgeNumber)

        //When
        databaseStore.accept(
            AnimeDatabaseStore.Intent.ChangeItemNewEpisodeStatus(
                isNewEpisode = false,
                id = FIRST_ID
            )
        )

        //Then
        assertEquals(0, controller.state.value.favoritesBadgeNumber)
    }

    @Test
    fun aTapOnATabReachesTheStoreAndComesBackAsALabel() = runTest {
        //Given
        val controller = startController()

        //When
        controller.accept(BottomNavigationBarStore.Intent.FavoritesSectionClick)

        //Then
        assertEquals<List<*>>(
            listOf(BottomNavigationBarStore.Label.NavigateToFavorites),
            labels
        )
    }

    @Test
    fun aSectionChangeFromTheHostReachesTheStateAsTheSelectedSection() = runTest {
        //Given
        val controller = startController()

        //When
        mainStore.accept(
            BottomNavigationBarStore.Intent.ChangeSelectedSection(SectionDomain.FAVORITES)
        )

        //Then
        assertEquals(SectionDomain.FAVORITES, controller.state.value.selectedSection)
    }

    @Test
    fun theBadgeStopsFollowingTheDatabaseWhileTheRootIsStopped() = runTest {
        //Given
        val controller = startController()
        lifecycle.stop()

        //When
        databaseStore.accept(
            AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(
                dbItem(id = SECOND_ID, isNewEpisode = true)
            )
        )

        //Then
        assertEquals(0, controller.state.value.favoritesBadgeNumber)
    }

    @Test
    fun theStateStopsFollowingTheStoreOnceTheRootIsDestroyed() = runTest {
        //Given
        val controller = startController()
        lifecycle.destroy()

        //When
        mainStore.accept(
            BottomNavigationBarStore.Intent.ChangeSelectedSection(SectionDomain.FAVORITES)
        )

        //Then
        assertEquals(SectionDomain.MAIN, controller.state.value.selectedSection)
    }
}
