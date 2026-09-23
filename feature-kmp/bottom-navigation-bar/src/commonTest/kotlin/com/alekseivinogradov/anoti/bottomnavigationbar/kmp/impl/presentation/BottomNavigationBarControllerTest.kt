package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.fake.AnimeDatabaseStoreFake
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.model.SectionDomain
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.BottomNavigationBarView
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.BottomNavigationBarUiModel
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.SectionUi
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.domain.store.BottomNavigationBarStoreFactory
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.essenty.lifecycle.stop
import com.arkivanov.mvikotlin.core.view.BaseMviView
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
import kotlin.test.assertTrue

private const val FIRST_ID = 11
private const val SECOND_ID = 22

@OptIn(ExperimentalCoroutinesApi::class)
class BottomNavigationBarControllerTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val screenLifecycle = LifecycleRegistry()

    private val viewLifecycle = LifecycleRegistry()

    private val view = BottomNavigationBarViewFake()

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
        if (screenLifecycle.state != Lifecycle.State.DESTROYED) {
            screenLifecycle.destroy()
        }
        Dispatchers.resetMain()
    }

    private fun startController() {
        BottomNavigationBarController(
            lifecycle = screenLifecycle,
            mainStore = mainStore,
            animeDatabaseStore = databaseStore
        ).onViewCreated(mainView = view, viewLifecycle = viewLifecycle)
        screenLifecycle.create()
        viewLifecycle.create()
        viewLifecycle.start()
        viewLifecycle.resume()
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
    fun theViewIsRenderedWithTheOpeningStateAsSoonAsItIsBound() = runTest {
        //Given
        val expected = BottomNavigationBarUiModel(SectionUi.MAIN, 0)

        //When
        startController()

        //Then
        assertEquals(listOf(expected), view.renderedModels)
    }

    @Test
    fun anItemWithANewEpisodeReachesTheViewAsABadgeNumber() = runTest {
        //Given
        startController()

        //When
        databaseStore.accept(
            AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(
                dbItem(id = FIRST_ID, isNewEpisode = true)
            )
        )

        //Then
        assertEquals(1, view.renderedModels.last().favoritesBadgeNumber)
    }

    @Test
    fun clearingTheLastNewEpisodeTakesTheBadgeBackToZero() = runTest {
        //Given
        startController()
        databaseStore.accept(
            AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(
                dbItem(id = FIRST_ID, isNewEpisode = true)
            )
        )
        assertEquals(1, view.renderedModels.last().favoritesBadgeNumber)

        //When
        databaseStore.accept(
            AnimeDatabaseStore.Intent.ChangeItemNewEpisodeStatus(
                isNewEpisode = false,
                id = FIRST_ID
            )
        )

        //Then
        assertEquals(0, view.renderedModels.last().favoritesBadgeNumber)
    }

    @Test
    fun aDatabaseChangeThatLeavesTheBadgeNumberAloneDoesNotRenderAgain() = runTest {
        //Given
        startController()
        databaseStore.accept(
            AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(
                dbItem(id = FIRST_ID, isNewEpisode = true)
            )
        )
        val rendersBefore = view.renderedModels.size

        //When
        databaseStore.accept(
            AnimeDatabaseStore.Intent.UpdateAnimeDatabaseItem(
                dbItem(id = FIRST_ID, isNewEpisode = true, name = "Frieren, renamed")
            )
        )

        //Then
        assertEquals(rendersBefore, view.renderedModels.size)
    }

    @Test
    fun aTapOnATabReachesTheStoreAndComesBackAsALabel() = runTest {
        //Given
        startController()

        //When
        view.dispatch(BottomNavigationBarStore.Intent.FavoritesSectionClick)

        //Then
        assertEquals(
            listOf(BottomNavigationBarStore.Label.NavigateToFavorites),
            view.handledLabels
        )
    }

    @Test
    fun aSectionChangeFromTheHostReachesTheViewAsTheSelectedSection() = runTest {
        //Given
        startController()

        //When
        mainStore.accept(
            BottomNavigationBarStore.Intent.ChangeSelectedSection(SectionDomain.FAVORITES)
        )

        //Then
        assertEquals(SectionUi.FAVORITES, view.renderedModels.last().selectedSection)
    }

    @Test
    fun theBadgeStopsFollowingTheDatabaseWhileTheViewIsStopped() = runTest {
        //Given
        startController()
        viewLifecycle.stop()
        val rendersBefore = view.renderedModels.size

        //When
        databaseStore.accept(
            AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(
                dbItem(id = SECOND_ID, isNewEpisode = true)
            )
        )

        //Then
        assertEquals(rendersBefore, view.renderedModels.size)
    }

    @Test
    fun destroyingTheScreenDisposesBothStores() = runTest {
        //Given
        startController()

        //When
        screenLifecycle.destroy()

        //Then
        assertTrue(mainStore.isDisposed)
        assertTrue(databaseStore.isDisposed)
    }
}

private class BottomNavigationBarViewFake :
    BaseMviView<BottomNavigationBarUiModel, BottomNavigationBarStore.Intent>(),
    BottomNavigationBarView {

    val renderedModels = mutableListOf<BottomNavigationBarUiModel>()

    val handledLabels: List<BottomNavigationBarStore.Label>
        field = mutableListOf<BottomNavigationBarStore.Label>()

    override fun render(model: BottomNavigationBarUiModel) {
        renderedModels += model
    }

    override fun handle(label: BottomNavigationBarStore.Label) {
        handledLabels += label
    }
}
