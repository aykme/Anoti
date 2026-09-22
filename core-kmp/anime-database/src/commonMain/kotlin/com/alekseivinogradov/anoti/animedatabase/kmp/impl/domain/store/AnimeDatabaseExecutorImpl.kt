package com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseExecutor
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.wrapper.AnimeDatabaseUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// One function per Intent handled, plus two helpers.
@Suppress("TooManyFunctions")
class AnimeDatabaseExecutorImpl(
    coroutineContextProvider: CoroutineContextProvider,
    private val usecases: AnimeDatabaseUsecases
) : AnimeDatabaseExecutor(
    mainContext = coroutineContextProvider.newMainCoroutineContext()
) {

    /**
     * Where writes run. It outlives this executor, so a screen destroyed mid-write still gets
     * its write finished. Reads use [scope] instead: nothing is left to render them.
     */
    private val writeScope = CoroutineScope(coroutineContextProvider.appMainCoroutineContext)

    private var fetchAllDatabaseItemsJob: Job? = null

    // An id is in one of these only while its write is running. Holding the finished Job instead
    // would keep every id ever written alive for the executor's whole lifetime.
    private val insertsInFlight: MutableSet<AnimeId> = mutableSetOf()
    private val deletesInFlight: MutableSet<AnimeId> = mutableSetOf()
    private val newEpisodeStatusChangesInFlight: MutableSet<AnimeId> = mutableSetOf()
    private val updatesInFlight: MutableSet<AnimeId> = mutableSetOf()

    private var resetAllItemsNewEpisodeStatusJob: Job? = null
    private var resetAllItemsExtraInfoJob: Job? = null

    override fun executeAction(action: AnimeDatabaseStore.Action) {
        when (action) {
            AnimeDatabaseStore.Action.SubscribeToDatabase -> subscribeToDatabase()
        }
    }

    override fun executeIntent(intent: AnimeDatabaseStore.Intent) {
        when (intent) {
            is AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem -> {
                insertAnimeDatabaseItem(intent)
            }

            is AnimeDatabaseStore.Intent.DeleteAnimeDatabaseItem -> {
                deleteAnimeDatabaseItem(intent)
            }

            AnimeDatabaseStore.Intent.ResetAllItemsNewEpisodeStatus -> {
                resetAllItemsNewEpisodeStatus()
            }

            is AnimeDatabaseStore.Intent.ChangeItemNewEpisodeStatus -> {
                changeItemNewEpisodeStatus(intent)
            }

            is AnimeDatabaseStore.Intent.UpdateAnimeDatabaseItem -> {
                updateAnimeDatabaseItem(intent)
            }

            AnimeDatabaseStore.Intent.ResetAllItemsExtraInfo -> {
                resetAllItemsExtraInfo()
            }
        }
    }

    private fun subscribeToDatabase() {
        if (fetchAllDatabaseItemsJob?.isActive == true) return
        fetchAllDatabaseItemsJob = scope.launch {
            usecases.fetchAllAnimeDatabaseItemsFlowUsecase.execute()
                .collect { animeDbList: List<AnimeDbDomain> ->
                    dispatch(AnimeDatabaseStore.Message.UpdateAnimeDatabaseItems(animeDbList))
                }
        }
    }

    private fun insertAnimeDatabaseItem(
        intent: AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem
    ) {
        val id = intent.animeDatabaseItem.id
        if (id in insertsInFlight) return
        if (databaseContainsItem(id)) return
        launchWrite(id, insertsInFlight) {
            usecases.insertAnimeDatabaseItemUsecase.execute(intent.animeDatabaseItem)
        }
    }

    private fun deleteAnimeDatabaseItem(
        intent: AnimeDatabaseStore.Intent.DeleteAnimeDatabaseItem
    ) {
        if (intent.id in deletesInFlight) return
        if (!databaseContainsItem(intent.id)) return
        launchWrite(intent.id, deletesInFlight) {
            usecases.deleteAnimeDatabaseItemUsecase.execute(intent.id)
        }
    }

    private fun resetAllItemsNewEpisodeStatus() {
        if (resetAllItemsNewEpisodeStatusJob?.isActive == true) return
        resetAllItemsNewEpisodeStatusJob =
            writeScope.launch {
                usecases.resetAllAnimeDatabaseItemsNewEpisodeStatusUsecase.execute()
                publish(AnimeDatabaseStore.Label.ResetAllItemsNewEpisodeStatusWasFinished)
            }
    }

    private fun changeItemNewEpisodeStatus(
        intent: AnimeDatabaseStore.Intent.ChangeItemNewEpisodeStatus
    ) {
        if (intent.id in newEpisodeStatusChangesInFlight) return
        val isItemLabelledWithNewEpisode = state().animeDatabaseItems.any { animeDb: AnimeDbDomain ->
            animeDb.id == intent.id && animeDb.isNewEpisode
        }
        if (!isItemLabelledWithNewEpisode) return

        launchWrite(intent.id, newEpisodeStatusChangesInFlight) {
            usecases.changeAnimeDatabaseItemNewEpisodeStatusUsecase.execute(
                id = intent.id,
                isNewEpisode = intent.isNewEpisode
            )
        }
    }

    private fun updateAnimeDatabaseItem(
        intent: AnimeDatabaseStore.Intent.UpdateAnimeDatabaseItem
    ) {
        val id = intent.animeDatabaseItem.id
        if (id in updatesInFlight) return
        if (!databaseContainsItem(id)) return
        launchWrite(id, updatesInFlight) {
            usecases.updateAnimeDatabaseItemUsecase.execute(intent.animeDatabaseItem)
        }
    }

    private fun resetAllItemsExtraInfo() {
        if (resetAllItemsExtraInfoJob?.isActive == true) return
        resetAllItemsExtraInfoJob = writeScope.launch {
            usecases.resetAllAnimeDatabaseItemsExtraInfoUsecase.execute()
        }
    }

    /** Runs [write] on [writeScope], marking [id] busy in [inFlight] until it is done. */
    private fun launchWrite(
        id: AnimeId,
        inFlight: MutableSet<AnimeId>,
        write: suspend () -> Unit
    ) {
        inFlight += id
        // Hung off the job rather than a `finally` inside it: a coroutine whose scope is already
        // gone never runs its body at all, and the id would then stay busy for good.
        writeScope.launch { write() }.invokeOnCompletion { inFlight -= id }
    }

    private fun databaseContainsItem(id: AnimeId): Boolean {
        return state().animeDatabaseItems.any { animeDb: AnimeDbDomain -> animeDb.id == id }
    }
}
