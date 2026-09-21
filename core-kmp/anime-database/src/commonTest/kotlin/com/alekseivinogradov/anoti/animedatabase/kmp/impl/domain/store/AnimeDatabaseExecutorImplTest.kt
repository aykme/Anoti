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
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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

    private companion object {
        private const val WRITE_FAILURE = "write failure"
    }
}
