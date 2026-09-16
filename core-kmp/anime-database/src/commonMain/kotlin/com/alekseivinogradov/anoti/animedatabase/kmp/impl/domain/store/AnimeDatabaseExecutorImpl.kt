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

// One function per Intent handled, one helper, plus the temporary dispose() override that only
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
    private val insertDatabaseItemsJobMap: MutableMap<AnimeId, Job> = mutableMapOf()
    private val deleteDatabaseItemsJobMap: MutableMap<AnimeId, Job> = mutableMapOf()
    private var resetAllItemsNewEpisodeStatusJob: Job? = null
    private var resetAllItemsExtraInfoJob: Job? = null
    private val changeItemNewEpisodeStatusJobMap: MutableMap<AnimeId, Job> = mutableMapOf()
    private val updateItemJobMap: MutableMap<AnimeId, Job> = mutableMapOf()

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
        val isJobActive = insertDatabaseItemsJobMap[id]?.isActive == true
        val isAlreadyInDatabase = databaseContainsItem(id)
        DiagnosticLog.log(
            "db[$storeTag] insert.intent id=$id jobActive=$isJobActive inDb=$isAlreadyInDatabase"
        )
        if (isJobActive) return
        if (isAlreadyInDatabase) return
        val requestedAt = TimeSource.Monotonic.markNow()
        insertDatabaseItemsJobMap[id] =
            writeScope.launch {
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
        val isJobActive = deleteDatabaseItemsJobMap[intent.id]?.isActive == true
        val isInDatabase = databaseContainsItem(intent.id)
        DiagnosticLog.log(
            "db[$storeTag] delete.intent id=${intent.id} jobActive=$isJobActive inDb=$isInDatabase"
        )
        if (isJobActive) return
        if (!isInDatabase) return
        val requestedAt = TimeSource.Monotonic.markNow()
        deleteDatabaseItemsJobMap[intent.id] =
            writeScope.launch {
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
        if (changeItemNewEpisodeStatusJobMap[intent.id]?.isActive == true) return

        val isItemAlreadyWithoutNewEpisodeLabel = !(
            state().animeDatabaseItems
                .find { animeDb: AnimeDbDomain ->
                    animeDb.id == intent.id
                }?.isNewEpisode ?: false
            )

        if (isItemAlreadyWithoutNewEpisodeLabel) return

        changeItemNewEpisodeStatusJobMap[intent.id] =
            writeScope.launch {
                usecases.changeAnimeDatabaseItemNewEpisodeStatusUsecase.execute(
                    id = intent.id,
                    isNewEpisode = intent.isNewEpisode
                )
            }
    }

    private fun updateAnimeDatabaseItem(
        intent: AnimeDatabaseStore.Intent.UpdateAnimeDatabaseItem
    ) {
        if (updateItemJobMap[intent.animeDatabaseItem.id]?.isActive == true) return
        if (!databaseContainsItem(intent.animeDatabaseItem.id)) return
        updateItemJobMap[intent.animeDatabaseItem.id] =
            writeScope.launch {
                usecases.updateAnimeDatabaseItemUsecase.execute(intent.animeDatabaseItem)
            }
    }

    private fun resetAllItemsExtraInfo() {
        if (resetAllItemsExtraInfoJob?.isActive == true) return
        resetAllItemsExtraInfoJob = writeScope.launch {
            usecases.resetAllAnimeDatabaseItemsExtraInfoUsecase.execute()
        }
    }

    private fun databaseContainsItem(id: AnimeId): Boolean {
        return state().animeDatabaseItems.map { animeDb: AnimeDbDomain ->
            animeDb.id
        }.toSet().contains(id)
    }

    private companion object {
        private const val HEX_RADIX = 16
        private const val MAX_LOGGED_IDS = 30
    }
}
