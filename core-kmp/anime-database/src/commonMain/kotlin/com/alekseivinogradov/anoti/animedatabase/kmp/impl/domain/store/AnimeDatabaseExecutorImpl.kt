package com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseExecutor
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.wrapper.AnimeDatabaseUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.diagnostics.DiagnosticLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

// One function per Intent handled, two helpers, plus the temporary dispose() override that only
// exists to trace the store's lifetime.
@Suppress("TooManyFunctions")
class AnimeDatabaseExecutorImpl(
    coroutineContextProvider: CoroutineContextProvider,
    private val usecases: AnimeDatabaseUsecases
) : AnimeDatabaseExecutor(
    mainContext = coroutineContextProvider.newMainCoroutineContext()
) {

    // Every screen builds its own store, so lines from different instances have to be told apart.
    private val storeTag = hashCode().toString(HEX_RADIX)

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

    override fun dispose() {
        DiagnosticLog.log("db[$storeTag] dispose")
        super.dispose()
    }

    private fun subscribeToDatabase() {
        if (fetchAllDatabaseItemsJob?.isActive == true) return
        fetchAllDatabaseItemsJob = scope.launch {
            DiagnosticLog.log("db[$storeTag] subscribe.start")
            try {
                usecases.fetchAllAnimeDatabaseItemsFlowUsecase.execute()
                    .collect { animeDbList: List<AnimeDbDomain> ->
                        DiagnosticLog.log(
                            "db[$storeTag] emit size=${animeDbList.size} " +
                                "ids=${animeDbList.map(AnimeDbDomain::id).take(MAX_LOGGED_IDS)}"
                        )
                        dispatch(
                            AnimeDatabaseStore.Message.UpdateAnimeDatabaseItems(animeDbList)
                        )
                    }
            } finally {
                DiagnosticLog.log("db[$storeTag] subscribe.end")
            }
        }
    }

    private fun insertAnimeDatabaseItem(
        intent: AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem
    ) {
        val id = intent.animeDatabaseItem.id
        val isJobActive = id in insertsInFlight
        val isAlreadyInDatabase = databaseContainsItem(id)
        DiagnosticLog.log(
            "db[$storeTag] insert.intent id=$id jobActive=$isJobActive inDb=$isAlreadyInDatabase"
        )
        if (isJobActive) return
        if (isAlreadyInDatabase) return
        val requestedAt = TimeSource.Monotonic.markNow()
        launchWrite(id, insertsInFlight) {
            DiagnosticLog.log(
                "db[$storeTag] insert.started id=$id " +
                    "queuedMs=${requestedAt.elapsedNow().inWholeMilliseconds}"
            )
            usecases.insertAnimeDatabaseItemUsecase.execute(intent.animeDatabaseItem)
            DiagnosticLog.log(
                "db[$storeTag] insert.done id=$id " +
                    "totalMs=${requestedAt.elapsedNow().inWholeMilliseconds}"
            )
        }
    }

    private fun deleteAnimeDatabaseItem(
        intent: AnimeDatabaseStore.Intent.DeleteAnimeDatabaseItem
    ) {
        val isJobActive = intent.id in deletesInFlight
        val isInDatabase = databaseContainsItem(intent.id)
        DiagnosticLog.log(
            "db[$storeTag] delete.intent id=${intent.id} jobActive=$isJobActive inDb=$isInDatabase"
        )
        if (isJobActive) return
        if (!isInDatabase) return
        val requestedAt = TimeSource.Monotonic.markNow()
        launchWrite(intent.id, deletesInFlight) {
            DiagnosticLog.log(
                "db[$storeTag] delete.started id=${intent.id} " +
                    "queuedMs=${requestedAt.elapsedNow().inWholeMilliseconds}"
            )
            usecases.deleteAnimeDatabaseItemUsecase.execute(intent.id)
            DiagnosticLog.log(
                "db[$storeTag] delete.done id=${intent.id} " +
                    "totalMs=${requestedAt.elapsedNow().inWholeMilliseconds}"
            )
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
        writeScope.launch {
            try {
                write()
            } finally {
                inFlight -= id
            }
        }
    }

    private fun databaseContainsItem(id: AnimeId): Boolean {
        return state().animeDatabaseItems.any { animeDb: AnimeDbDomain -> animeDb.id == id }
    }

    private companion object {
        private const val HEX_RADIX = 16
        private const val MAX_LOGGED_IDS = 30
    }
}
