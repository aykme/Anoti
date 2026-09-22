package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
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
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
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

    private val view = FakeBottomNavigationBarView()

    private lateinit var mainStore: BottomNavigationBarStore

    private lateinit var databaseStore: AnimeDatabaseStore

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mainStore = BottomNavigationBarStoreFactory(DefaultStoreFactory()).create()
        databaseStore = FakeAnimeDatabaseStoreFactory(DefaultStoreFactory()).create()
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

private class FakeBottomNavigationBarView :
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

/**
 * An in-memory stand-in for the real database store. Same contract, backed by a list the test
 * mutates through the intents the store already accepts.
 */
private class FakeAnimeDatabaseStoreFactory(private val storeFactory: StoreFactory) {

    fun create(): AnimeDatabaseStore = object :
        AnimeDatabaseStore,
        Store<AnimeDatabaseStore.Intent, AnimeDatabaseStore.State, AnimeDatabaseStore.Label>
        by storeFactory.create(
            name = "FakeAnimeDatabaseStore",
            initialState = AnimeDatabaseStore.State(),
            executorFactory = ::FakeAnimeDatabaseExecutor,
            reducer = FakeAnimeDatabaseReducer
        ) {}
}

private class FakeAnimeDatabaseExecutor : CoroutineExecutor<
    AnimeDatabaseStore.Intent,
    AnimeDatabaseStore.Action,
    AnimeDatabaseStore.State,
    AnimeDatabaseStore.Message,
    AnimeDatabaseStore.Label
    >() {

    override fun executeIntent(intent: AnimeDatabaseStore.Intent) {
        val items = state().animeDatabaseItems
        when (intent) {
            is AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem ->
                replaceItems(items + intent.animeDatabaseItem)

            is AnimeDatabaseStore.Intent.DeleteAnimeDatabaseItem ->
                replaceItems(items.filterNot { it.id == intent.id })

            is AnimeDatabaseStore.Intent.UpdateAnimeDatabaseItem ->
                replaceItems(
                    items.map { item: AnimeDbDomain ->
                        if (item.id == intent.animeDatabaseItem.id) {
                            intent.animeDatabaseItem
                        } else {
                            item
                        }
                    }
                )

            is AnimeDatabaseStore.Intent.ChangeItemNewEpisodeStatus ->
                replaceItems(
                    items.map { item: AnimeDbDomain ->
                        if (item.id == intent.id) {
                            item.copy(isNewEpisode = intent.isNewEpisode)
                        } else {
                            item
                        }
                    }
                )

            AnimeDatabaseStore.Intent.ResetAllItemsNewEpisodeStatus ->
                replaceItems(items.map { it.copy(isNewEpisode = false) })

            AnimeDatabaseStore.Intent.ResetAllItemsExtraInfo ->
                replaceItems(items.map { it.copy(isExtraInfoEnabled = false) })
        }
    }

    private fun replaceItems(items: List<AnimeDbDomain>) {
        dispatch(AnimeDatabaseStore.Message.UpdateAnimeDatabaseItems(items))
    }
}

private object FakeAnimeDatabaseReducer :
    Reducer<AnimeDatabaseStore.State, AnimeDatabaseStore.Message> {

    override fun AnimeDatabaseStore.State.reduce(
        msg: AnimeDatabaseStore.Message
    ): AnimeDatabaseStore.State = when (msg) {
        is AnimeDatabaseStore.Message.UpdateAnimeDatabaseItems ->
            copy(animeDatabaseItems = msg.animeDatabaseItems)
    }
}
