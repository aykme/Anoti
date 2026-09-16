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

// One function per Intent handled, plus two helpers, not incidental growth.
@Suppress("TooManyFunctions")
class AnimeDatabaseExecutorImpl(
    private val coroutineContextProvider: CoroutineContextProvider,
    private val usecases: AnimeDatabaseUsecases
) : AnimeDatabaseExecutor(
    mainContext = coroutineContextProvider.newMainCoroutineContext()
) {

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
            launchWrite {
                usecases.insertAnimeDatabaseItemUsecase.execute(intent.animeDatabaseItem)
            }
    }

    private fun deleteAnimeDatabaseItem(
        intent: AnimeDatabaseStore.Intent.DeleteAnimeDatabaseItem
    ) {
        if (deleteDatabaseItemsJobMap[intent.id]?.isActive == true) return
        if (!databaseContainsItem(intent.id)) return
        deleteDatabaseItemsJobMap[intent.id] =
            launchWrite {
                usecases.deleteAnimeDatabaseItemUsecase.execute(intent.id)
            }
    }

    private fun resetAllItemsNewEpisodeStatus() {
        if (resetAllItemsNewEpisodeStatusJob?.isActive == true) return
        resetAllItemsNewEpisodeStatusJob =
            launchWrite {
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
            launchWrite {
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
            launchWrite {
                usecases.updateAnimeDatabaseItemUsecase.execute(intent.animeDatabaseItem)
            }
    }

    private fun resetAllItemsExtraInfo() {
        if (resetAllItemsExtraInfoJob?.isActive == true) return
        resetAllItemsExtraInfoJob = launchWrite {
            usecases.resetAllAnimeDatabaseItemsExtraInfoUsecase.execute()
        }
    }

    /**
     * Runs a write outside this executor's scope, so it still finishes when the screen that
     * started it is destroyed mid-write. Reads are scoped to the executor instead: nothing is
     * left to render them.
     */
    private fun launchWrite(block: suspend CoroutineScope.() -> Unit): Job =
        scope.launch(context = coroutineContextProvider.appMainCoroutineContext, block = block)

    private fun databaseContainsItem(id: AnimeId): Boolean {
        return state().animeDatabaseItems.map { animeDb: AnimeDbDomain ->
            animeDb.id
        }.toSet().contains(id)
    }
}
