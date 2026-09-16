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

    private fun subscribeToDatabase() {
        if (fetchAllDatabaseItemsJob?.isActive == true) return
        fetchAllDatabaseItemsJob = scope.launch {
            usecases.fetchAllAnimeDatabaseItemsFlowUsecase.execute()
                .collect { animeDbList: List<AnimeDbDomain> ->
                    dispatch(
                        AnimeDatabaseStore.Message.UpdateAnimeDatabaseItems(animeDbList)
                    )
                }
        }
    }

    private fun insertAnimeDatabaseItem(
        intent: AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem
    ) {
        if (insertDatabaseItemsJobMap[intent.animeDatabaseItem.id]?.isActive == true) return
        if (databaseContainsItem(intent.animeDatabaseItem.id)) return
        insertDatabaseItemsJobMap[intent.animeDatabaseItem.id] =
            writeScope.launch {
                usecases.insertAnimeDatabaseItemUsecase.execute(intent.animeDatabaseItem)
            }
    }

    private fun deleteAnimeDatabaseItem(
        intent: AnimeDatabaseStore.Intent.DeleteAnimeDatabaseItem
    ) {
        if (deleteDatabaseItemsJobMap[intent.id]?.isActive == true) return
        if (!databaseContainsItem(intent.id)) return
        deleteDatabaseItemsJobMap[intent.id] =
            writeScope.launch {
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
}
