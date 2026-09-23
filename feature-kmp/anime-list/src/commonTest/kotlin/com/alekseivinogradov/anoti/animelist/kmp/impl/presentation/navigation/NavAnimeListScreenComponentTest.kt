package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.navigation

import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ANIME_LIST_APPEND_URL
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animebase.kmp.impl.data.service.ShikimoriApiServiceImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.AnimeDatabaseExecutorImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store.AnimeDatabaseStoreFactory
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.fake.AnimeDatabaseUsecasesFake
import com.alekseivinogradov.anoti.animelist.kmp.api.di.DiAnimeListDependencies
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SearchDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionHatDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.impl.di.createDiAnimeListComponent
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.fake.CoroutineContextProviderFake
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.formatter.fake.DateFormatterFake
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.alekseivinogradov.anoti.network.kmp.impl.data.client.createHttpClient
import com.alekseivinogradov.anoti.network.kmp.impl.data.fake.SafeApiFake
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NavAnimeListScreenComponentTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val lifecycles = mutableListOf<LifecycleRegistry>()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        lifecycles.filter { it.state != Lifecycle.State.DESTROYED }.forEach { it.destroy() }
        Dispatchers.resetMain()
    }

    /** Answers every list request with the same page, and every details request with a date. */

    private class DiAnimeListDependenciesFake(
        override val animeDatabaseStore: AnimeDatabaseStore,
        override val coroutineContextProvider: CoroutineContextProvider
    ) : DiAnimeListDependencies {
        override val storeFactory: StoreFactory = DefaultStoreFactory()
        override val systemMessageProvider = SystemMessageProvider(
            makeConnectionErrorSystemMessage = {},
            makeUnknownErrorSystemMessage = {}
        )
        override val dateFormatter: DateFormatter = DateFormatterFake()
        override val shikimoriApiService: ShikimoriApiService =
            ShikimoriApiServiceImpl(createHttpClient(singlePageCatalog()))
        override val safeApi: SafeApi = SafeApiFake()
    }

    /** One component and the state keeper it consumes from and saves through. */
    private class Wiring(
        val lifecycle: LifecycleRegistry,
        val stateKeeper: StateKeeperDispatcher,
        val component: NavAnimeListScreenComponent
    )

    private val databaseUsecases = AnimeDatabaseUsecasesFake().usecases

    private fun createDatabaseStore(
        coroutineContextProvider: CoroutineContextProvider
    ): AnimeDatabaseStore = AnimeDatabaseStoreFactory(
        storeFactory = DefaultStoreFactory(),
        executorFactory = {
            AnimeDatabaseExecutorImpl(
                coroutineContextProvider = coroutineContextProvider,
                usecases = databaseUsecases
            )
        }
    ).create()

    private fun createWiring(savedState: SerializableContainer? = null): Wiring {
        val coroutineContextProvider = CoroutineContextProviderFake()
        val lifecycle = LifecycleRegistry().also(lifecycles::add)
        val stateKeeper = StateKeeperDispatcher(savedState)
        val component = NavAnimeListScreenComponent(
            componentContext = DefaultComponentContext(
                lifecycle = lifecycle,
                stateKeeper = stateKeeper
            ),
            diAnimeListComponent = createDiAnimeListComponent(
                parent = DiAnimeListDependenciesFake(
                    animeDatabaseStore = createDatabaseStore(coroutineContextProvider),
                    coroutineContextProvider = coroutineContextProvider
                )
            )
        )
        lifecycle.resume()
        return Wiring(lifecycle = lifecycle, stateKeeper = stateKeeper, component = component)
    }

    private suspend fun awaitOngoingLoaded(component: NavAnimeListScreenComponent) {
        component.ongoingSectionStore.states.first {
            it.sectionContent.contentType == ContentTypeDomain.LOADED
        }
    }

    /** Leaves every section loaded, with its first item showing the extra episode info. */
    private suspend fun openAndExpandEverySection(component: NavAnimeListScreenComponent) {
        awaitOngoingLoaded(component)
        component.ongoingSectionStore.accept(
            OngoingSectionStore.Intent.EpisodesInfoClick(EXPANDED_ITEM_ID)
        )
        component.announcedSectionStore.accept(AnnouncedSectionStore.Intent.OpenSection)
        component.announcedSectionStore.states.first {
            it.sectionContent.contentType == ContentTypeDomain.LOADED
        }
        component.announcedSectionStore.accept(
            AnnouncedSectionStore.Intent.EpisodesInfoClick(EXPANDED_ITEM_ID)
        )
        // Before OpenSection: the search flow is only seeded once the section opens, so a later
        // change would run through the debounce and reload the section.
        component.searchSectionStore.accept(
            SearchSectionStore.Intent.ChangeSearchText(SEARCH_TEXT)
        )
        component.searchSectionStore.accept(SearchSectionStore.Intent.OpenSection)
        component.searchSectionStore.states.first {
            it.sectionContent.contentType == ContentTypeDomain.LOADED
        }
        component.searchSectionStore.accept(
            SearchSectionStore.Intent.EpisodesInfoClick(EXPANDED_ITEM_ID)
        )
        awaitExpandedDetails(component)
    }

    /**
     * The details behind an expanded item arrive after the list it sits in. Only the sections
     * that fetch them are waited on; the upcoming one shows no next-episode date.
     */
    private suspend fun awaitExpandedDetails(component: NavAnimeListScreenComponent) {
        component.ongoingSectionStore.states.first {
            it.sectionContent.animeDetails.nextEpisodesInfo.containsKey(EXPANDED_ITEM_ID)
        }
        component.searchSectionStore.states.first {
            it.sectionContent.animeDetails.nextEpisodesInfo.containsKey(EXPANDED_ITEM_ID)
        }
    }

    @Test
    fun aComponentWithNoSavedStateOpensTheOngoingSection() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring()

        //When
        wiring.component.applyRestoredStateIfAny()
        awaitOngoingLoaded(wiring.component)

        //Then
        assertEquals(SectionHatDomain.ONGOINGS, wiring.component.mainStore.state.selectedSection)
        assertEquals(
            PAGE_ITEM_COUNT,
            wiring.component.ongoingSectionStore.state.sectionContent.listItems.size
        )
        assertEquals(
            ContentTypeDomain.LOADING,
            wiring.component.announcedSectionStore.state.sectionContent.contentType
        )
        assertEquals(
            ContentTypeDomain.LOADING,
            wiring.component.searchSectionStore.state.sectionContent.contentType
        )
    }

    @Test
    fun theSavedStateReplaysTheSelectedSectionAndItsSearch() = runTest(testDispatcher) {
        //Given
        val beforeProcessDeath = createWiring()
        beforeProcessDeath.component.applyRestoredStateIfAny()
        beforeProcessDeath.component.mainStore.accept(
            AnimeListMainStore.Intent.SearchSectionClick
        )
        beforeProcessDeath.component.mainStore.accept(
            AnimeListMainStore.Intent.ChangeSearchText(SEARCH_TEXT)
        )
        beforeProcessDeath.component.mainStore.accept(AnimeListMainStore.Intent.CancelSearchClick)
        val savedState = beforeProcessDeath.stateKeeper.save()

        //When
        val afterProcessDeath = createWiring(savedState = savedState)
        afterProcessDeath.component.applyRestoredStateIfAny()

        //Then
        val mainState = afterProcessDeath.component.mainStore.state
        assertEquals(SectionHatDomain.SEARCH, mainState.selectedSection)
        assertEquals(SEARCH_TEXT, mainState.search.searchText)
        assertEquals(
            SearchDomain.Type.HIDDEN,
            mainState.search.type,
            "a search bar hidden before the restore must not come back shown"
        )
        assertEquals(
            SEARCH_TEXT,
            afterProcessDeath.component.searchSectionStore.state.searchText
        )
    }

    @Test
    fun theSavedStateReplaysEverySectionsItemCountEnabledIdsAndNextEpisodeInfo() =
        runTest(testDispatcher) {
            //Given
            val beforeProcessDeath = createWiring()
            beforeProcessDeath.component.applyRestoredStateIfAny()
            openAndExpandEverySection(beforeProcessDeath.component)
            val savedState = beforeProcessDeath.stateKeeper.save()

            //When
            val afterProcessDeath = createWiring(savedState = savedState)
            afterProcessDeath.component.applyRestoredStateIfAny()
            // The restored section reloads its list and refetches the expanded item's details,
            // so the state to assert on is the one where both have landed, not whichever is
            // current once the wait returns.
            val ongoing = afterProcessDeath.component.ongoingSectionStore.states.first {
                it.sectionContent.contentType == ContentTypeDomain.LOADED &&
                    it.sectionContent.animeDetails.nextEpisodesInfo.containsKey(EXPANDED_ITEM_ID)
            }

            //Then
            assertEquals(PAGE_ITEM_COUNT, ongoing.sectionContent.listItems.size)
            assertEquals(setOf(EXPANDED_ITEM_ID), ongoing.sectionContent.enabledExtraEpisodesInfoIds)
            assertEquals(
                mapOf<AnimeId, String?>(EXPANDED_ITEM_ID to NEXT_EPISODE_AT),
                ongoing.sectionContent.animeDetails.nextEpisodesInfo
            )
            // The sections the user was not on page their items back in only once they open, so
            // their saved count comes back as the target that opening will page up to.
            val announced = afterProcessDeath.component.announcedSectionStore.state
            assertEquals(PAGE_ITEM_COUNT, announced.restoreTargetItemCount)
            assertEquals(
                setOf(EXPANDED_ITEM_ID),
                announced.sectionContent.enabledExtraEpisodesInfoIds
            )
            val search = afterProcessDeath.component.searchSectionStore.state
            assertEquals(PAGE_ITEM_COUNT, search.restoreTargetItemCount)
            assertEquals(setOf(EXPANDED_ITEM_ID), search.sectionContent.enabledExtraEpisodesInfoIds)
            assertEquals(
                mapOf<AnimeId, String?>(EXPANDED_ITEM_ID to NEXT_EPISODE_AT),
                search.sectionContent.animeDetails.nextEpisodesInfo
            )
        }

    @Test
    fun destroyingTheLifecycleDisposesEveryStoreTheComponentOwns() = runTest(testDispatcher) {
        //Given
        val wiring = createWiring()

        //When
        wiring.lifecycle.destroy()

        //Then
        val component = wiring.component
        assertTrue(component.mainStore.isDisposed, "the main store outlived its screen")
        assertTrue(component.ongoingSectionStore.isDisposed, "the ongoing store outlived its screen")
        assertTrue(
            component.announcedSectionStore.isDisposed,
            "the announced store outlived its screen"
        )
        assertTrue(component.searchSectionStore.isDisposed, "the search store outlived its screen")
        assertTrue(component.animeDatabaseStore.isDisposed, "the database store outlived its screen")
    }
}

private const val PAGE_ITEM_COUNT = 3

private const val EXPANDED_ITEM_ID = 1

private const val NEXT_EPISODE_AT = "2024-01-05T10:00:00+03:00"

private const val SEARCH_TEXT = "totoro"

/**
 * Answers every listing with one full page of the same items, and every details call with the
 * anime asked for, so the screen always has something to show.
 */
private fun singlePageCatalog() = MockEngine { request ->
    val path = request.url.encodedPath
    val body = if (path.endsWith("/$ANIME_LIST_APPEND_URL")) {
        (1..PAGE_ITEM_COUNT).joinToString(prefix = "[", postfix = "]") { id: Int ->
            """{"id": $id, "name": "Item $id", "status": "ongoing"}"""
        }
    } else {
        val id = path.substringAfterLast(delimiter = "/")
        """{"id": $id, "name": "Item $id", """ +
            """"next_episode_at": "$NEXT_EPISODE_AT", "status": "ongoing"}"""
    }
    respond(
        content = ByteReadChannel(body),
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
    )
}
