package com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.wrapper.AnimeDatabaseUsecases
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.fake.AnimeDaoFake
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.repository.AnimeDatabaseRepositoryImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.ChangeAnimeDatabaseItemNewEpisodeStatusUsecaseImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.DeleteAnimeDatabaseItemUsecaseImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.FetchAllAnimeDatabaseItemsFlowUsecaseImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.InsertAnimeDatabaseItemUsecaseImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.ResetAllAnimeDatabaseItemsExtraInfoUsecaseImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecaseImpl
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.UpdateAnimeDatabaseItemUsecaseImpl
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderBase
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeDatabaseExecutorImplTest {

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

    private fun sample(id: AnimeId) = AnimeDbDomain(
        id = id,
        imageUrl = null,
        name = "Anime $id",
        episodesAired = null,
        episodesTotal = null,
        nextEpisodeAt = null,
        airedOn = null,
        releasedOn = null,
        score = null,
        releaseStatus = ReleaseStatusDb.ONGOING,
        episodesViewed = 0,
        isNewEpisode = false
    )

    private fun createStore(
        dao: AnimeDaoFake,
        onUncaughtThrowable: (Throwable) -> Unit = {}
    ): AnimeDatabaseStore {
        val repository = AnimeDatabaseRepositoryImpl(dao)
        val coroutineContextProvider = object : CoroutineContextProviderBase() {
            override val exceptionHandlerCallback: (Throwable) -> Unit = onUncaughtThrowable
        }
        val usecases = AnimeDatabaseUsecases(
            fetchAllAnimeDatabaseItemsFlowUsecase =
            FetchAllAnimeDatabaseItemsFlowUsecaseImpl(repository),
            insertAnimeDatabaseItemUsecase = InsertAnimeDatabaseItemUsecaseImpl(repository),
            deleteAnimeDatabaseItemUsecase = DeleteAnimeDatabaseItemUsecaseImpl(repository),
            resetAllAnimeDatabaseItemsNewEpisodeStatusUsecase =
            ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecaseImpl(repository),
            changeAnimeDatabaseItemNewEpisodeStatusUsecase =
            ChangeAnimeDatabaseItemNewEpisodeStatusUsecaseImpl(repository),
            updateAnimeDatabaseItemUsecase = UpdateAnimeDatabaseItemUsecaseImpl(repository),
            resetAllAnimeDatabaseItemsExtraInfoUsecase =
            ResetAllAnimeDatabaseItemsExtraInfoUsecaseImpl(repository)
        )
        return AnimeDatabaseStoreFactory(
            storeFactory = DefaultStoreFactory(),
            executorFactory = {
                AnimeDatabaseExecutorImpl(
                    coroutineContextProvider = coroutineContextProvider,
                    usecases = usecases
                )
            }
        ).create().also(createdStores::add)
    }

    @Test
    fun disposingTheStoreEndsTheDatabaseSubscription() = runTest(testDispatcher) {
        //Given
        val dao = AnimeDaoFake()
        val store = createStore(dao)
        assertEquals(1, dao.subscriptionCount.value, "the store should collect the database flow")

        //When
        store.dispose()

        //Then
        assertEquals(0, dao.subscriptionCount.value, "the subscription outlived its store")
    }

    @Test
    fun aWriteInFlightWhenTheStoreIsDisposedStillReachesTheDatabase() = runTest(testDispatcher) {
        //Given
        val writeGate = CompletableDeferred<Unit>()
        val dao = AnimeDaoFake(beforeWrite = { writeGate.await() })
        val store = createStore(dao)
        val item = sample(id = 1)
        store.accept(AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(item))

        //When
        store.dispose()
        writeGate.complete(Unit)
        advanceUntilIdle()

        //Then
        assertEquals(listOf(item.id), dao.getAllItems().map { it.id }, "the write was dropped")
    }

    @Test
    fun aSecondInsertOfTheSameItemIsIgnoredWhileTheFirstIsStillInFlight() = runTest(testDispatcher) {
        //Given
        var writeAttempts = 0
        val writeGate = CompletableDeferred<Unit>()
        val dao = AnimeDaoFake(
            beforeWrite = {
                writeAttempts++
                writeGate.await()
            }
        )
        val store = createStore(dao)
        val item = sample(id = 1)

        //When
        store.accept(AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(item))
        store.accept(AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(item))
        writeGate.complete(Unit)
        advanceUntilIdle()

        //Then
        assertEquals(1, writeAttempts, "the in-flight write should swallow the repeated intent")
    }

    @Test
    fun aFailedWriteIsReportedAndLeavesTheStoreRunning() = runTest(testDispatcher) {
        //Given
        val caught = mutableListOf<Throwable>()
        val dao = AnimeDaoFake(beforeWrite = { throw IllegalStateException(WRITE_FAILURE) })
        val store = createStore(dao, onUncaughtThrowable = { caught += it })

        //When
        store.accept(AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(sample(id = 1)))

        //Then
        assertEquals(WRITE_FAILURE, caught.singleOrNull()?.message, "the failure went unreported")
        assertEquals(1, dao.subscriptionCount.value, "the failed write took the store down")
    }

    @Test
    fun insertPutsTheItemInTheDatabase() = runTest(testDispatcher) {
        //Given
        val dao = AnimeDaoFake()
        val store = createStore(dao)
        val item = sample(id = 1)

        //When
        store.accept(AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(item))
        advanceUntilIdle()

        //Then
        assertEquals(listOf(item.id), dao.getAllItems().map { it.id })
    }

    @Test
    fun insertOfAnItemAlreadyInTheDatabaseIsIgnored() = runTest(testDispatcher) {
        //Given
        val dao = AnimeDaoFake()
        val store = createStore(dao)
        val item = sample(id = 1)
        store.accept(AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(item))
        advanceUntilIdle()
        var writeAttempts = 0
        dao.beforeWrite = { writeAttempts++ }

        //When
        store.accept(AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(item))
        advanceUntilIdle()

        //Then
        assertEquals(0, writeAttempts, "a stored item should not be written again")
    }

    @Test
    fun deleteRemovesTheItemFromTheDatabase() = runTest(testDispatcher) {
        //Given
        val dao = AnimeDaoFake()
        val store = createStore(dao)
        val item = sample(id = 1)
        store.accept(AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(item))
        advanceUntilIdle()

        //When
        store.accept(AnimeDatabaseStore.Intent.DeleteAnimeDatabaseItem(item.id))
        advanceUntilIdle()

        //Then
        assertEquals(emptyList(), dao.getAllItems())
    }

    @Test
    fun deleteOfAnItemThatIsNotStoredIsIgnored() = runTest(testDispatcher) {
        //Given
        var writeAttempts = 0
        val dao = AnimeDaoFake(beforeWrite = { writeAttempts++ })
        val store = createStore(dao)

        //When
        store.accept(AnimeDatabaseStore.Intent.DeleteAnimeDatabaseItem(id = ABSENT_ID))
        advanceUntilIdle()

        //Then
        assertEquals(0, writeAttempts, "an absent item should not reach the database")
    }

    @Test
    fun aSecondDeleteOfTheSameItemIsIgnoredWhileTheFirstIsStillInFlight() =
        runTest(testDispatcher) {
            //Given
            val dao = AnimeDaoFake()
            val store = createStore(dao)
            val item = sample(id = 1)
            store.accept(AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(item))
            advanceUntilIdle()
            var deleteAttempts = 0
            val deleteGate = CompletableDeferred<Unit>()
            dao.beforeWrite = {
                deleteAttempts++
                deleteGate.await()
            }

            //When
            store.accept(AnimeDatabaseStore.Intent.DeleteAnimeDatabaseItem(item.id))
            store.accept(AnimeDatabaseStore.Intent.DeleteAnimeDatabaseItem(item.id))
            deleteGate.complete(Unit)
            advanceUntilIdle()

            //Then
            assertEquals(1, deleteAttempts, "the in-flight delete should swallow the repeat")
        }

    @Test
    fun updateReplacesTheStoredItem() = runTest(testDispatcher) {
        //Given
        val dao = AnimeDaoFake()
        val store = createStore(dao)
        val item = sample(id = 1)
        store.accept(AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(item))
        advanceUntilIdle()

        //When
        store.accept(
            AnimeDatabaseStore.Intent.UpdateAnimeDatabaseItem(item.copy(episodesViewed = 7))
        )
        advanceUntilIdle()

        //Then
        assertEquals(listOf(7), dao.getAllItems().map { it.episodesViewed })
    }

    @Test
    fun updateOfAnItemThatIsNotStoredIsIgnored() = runTest(testDispatcher) {
        //Given
        var writeAttempts = 0
        val dao = AnimeDaoFake(beforeWrite = { writeAttempts++ })
        val store = createStore(dao)

        //When
        store.accept(
            AnimeDatabaseStore.Intent.UpdateAnimeDatabaseItem(sample(id = ABSENT_ID))
        )
        advanceUntilIdle()

        //Then
        assertEquals(0, writeAttempts, "an absent item should not reach the database")
    }

    @Test
    fun changeItemNewEpisodeStatusClearsTheFlagOnTheStoredItem() = runTest(testDispatcher) {
        //Given
        val dao = AnimeDaoFake()
        val store = createStore(dao)
        val item = sample(id = 1).copy(isNewEpisode = true)
        store.accept(AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(item))
        advanceUntilIdle()

        //When
        store.accept(
            AnimeDatabaseStore.Intent.ChangeItemNewEpisodeStatus(
                isNewEpisode = false,
                id = item.id
            )
        )
        advanceUntilIdle()

        //Then
        assertEquals(listOf(false), dao.getAllItems().map { it.isNewEpisode })
    }

    @Test
    fun changeItemNewEpisodeStatusIsIgnoredWhenTheItemHasNoNewEpisodeAlready() =
        runTest(testDispatcher) {
            //Given
            val dao = AnimeDaoFake()
            val store = createStore(dao)
            val item = sample(id = 1)
            store.accept(AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(item))
            advanceUntilIdle()
            var writeAttempts = 0
            dao.beforeWrite = { writeAttempts++ }

            //When
            store.accept(
                AnimeDatabaseStore.Intent.ChangeItemNewEpisodeStatus(
                    isNewEpisode = false,
                    id = item.id
                )
            )
            advanceUntilIdle()

            //Then
            assertEquals(0, writeAttempts, "a cleared flag should not be written again")
        }

    @Test
    fun resetAllItemsNewEpisodeStatusClearsEveryFlagAndPublishesItsLabel() =
        runTest(testDispatcher) {
            //Given
            val dao = AnimeDaoFake()
            val store = createStore(dao)
            val published = mutableListOf<AnimeDatabaseStore.Label>()
            backgroundScope.launch { store.labels.collect(published::add) }
            store.accept(
                AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(
                    sample(id = 1).copy(isNewEpisode = true)
                )
            )
            store.accept(
                AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(
                    sample(id = 2).copy(isNewEpisode = true)
                )
            )
            advanceUntilIdle()

            //When
            store.accept(AnimeDatabaseStore.Intent.ResetAllItemsNewEpisodeStatus)
            advanceUntilIdle()

            //Then
            assertEquals(listOf(false, false), dao.getAllItems().map { it.isNewEpisode })
            assertEquals<List<AnimeDatabaseStore.Label>>(
                listOf(AnimeDatabaseStore.Label.ResetAllItemsNewEpisodeStatusWasFinished),
                published
            )
        }

    @Test
    fun resetAllItemsExtraInfoClearsTheModeAndTheNextEpisodeDate() = runTest(testDispatcher) {
        //Given
        val dao = AnimeDaoFake()
        val store = createStore(dao)
        val item = sample(id = 1)
            .copy(isExtraInfoEnabled = true, nextEpisodeAt = NEXT_EPISODE_AT)
        store.accept(AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(item))
        advanceUntilIdle()

        //When
        store.accept(AnimeDatabaseStore.Intent.ResetAllItemsExtraInfo)
        advanceUntilIdle()

        //Then
        val stored = dao.getAllItems().single()
        assertEquals(false, stored.isExtraInfoEnabled)
        assertNull(stored.nextEpisodeAt)
    }

    @Test
    fun theSameItemIsWrittenAgainOnceTheEarlierWriteHasFinished() = runTest(testDispatcher) {
        //Given
        var writeAttempts = 0
        val dao = AnimeDaoFake(beforeWrite = { writeAttempts++ })
        val store = createStore(dao)
        val item = sample(id = 1)
        store.accept(AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(item))
        advanceUntilIdle()
        store.accept(AnimeDatabaseStore.Intent.DeleteAnimeDatabaseItem(item.id))
        advanceUntilIdle()

        //When
        store.accept(AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(item))
        advanceUntilIdle()

        //Then
        // Three writes, not two: a finished write must not keep blocking the id it wrote.
        assertEquals(3, writeAttempts)
    }

    @Test
    fun theStateFollowsTheDatabase() = runTest(testDispatcher) {
        //Given
        val dao = AnimeDaoFake()
        val store = createStore(dao)
        val item = sample(id = 1)

        //When
        store.accept(AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(item))
        advanceUntilIdle()

        //Then
        assertEquals(listOf(item), store.state.animeDatabaseItems)
    }

    private companion object {
        private const val WRITE_FAILURE = "write failure"
        private const val ABSENT_ID = 404
        private const val NEXT_EPISODE_AT = "2026-09-22T12:00:00Z"
    }
}
