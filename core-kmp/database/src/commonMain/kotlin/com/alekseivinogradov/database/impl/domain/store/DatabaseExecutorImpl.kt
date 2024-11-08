package com.alekseivinogradov.database.impl.domain.store

import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.database.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.database.api.domain.store.DatabaseExecutor
import com.alekseivinogradov.database.api.domain.store.DatabaseStore
import com.alekseivinogradov.database.api.domain.usecase.wrapper.DatabaseUsecases
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DatabaseExecutorImpl(
    private val coroutineContextProvider: CoroutineContextProvider,
    private val usecases: DatabaseUsecases
) : DatabaseExecutor() {

    private var fetchAllDatabaseItemsJob: Job? = null
    private val insertAnimeDatabaseItemsJobMap: MutableMap<AnimeId, Job> = mutableMapOf()
    private val deleteAnimeDatabaseItemsJobMap: MutableMap<AnimeId, Job> = mutableMapOf()
    private var resetAllItemsNewEpisodeStatusJob: Job? = null
    private val changeItemNewEpisodeStatusJobMap: MutableMap<AnimeId, Job> = mutableMapOf()
    private val updateItemJobMap: MutableMap<AnimeId, Job> = mutableMapOf()

    override fun executeAction(action: DatabaseStore.Action) {
        when (action) {
            DatabaseStore.Action.SubscribeToDatabase -> subscribeToDatabase()
        }
    }

    override fun executeIntent(intent: DatabaseStore.Intent) {
        when (intent) {
            is DatabaseStore.Intent.InsertAnimeDatabaseItem -> insertAnimeDatabaseItem(intent)
            is DatabaseStore.Intent.DeleteAnimeDatabaseItem -> deleteAnimeDatabaseItem(intent)
            DatabaseStore.Intent.ResetAllItemsNewEpisodeStatus -> resetAllItemsNewEpisodeStatus()
            is DatabaseStore.Intent.ChangeItemNewEpisodeStatus -> {
                changeItemNewEpisodeStatus(intent)
            }

            is DatabaseStore.Intent.UpdateAnimeDatabaseItem -> updateAnimeDatabaseItem(intent)
        }
    }

    private fun subscribeToDatabase() {
        if (fetchAllDatabaseItemsJob?.isActive == true) return
        fetchAllDatabaseItemsJob = scope.launch(coroutineContextProvider.mainCoroutineContext) {
            usecases.fetchAllDatabaseItemsFlowUsecase.execute()
                .collect { animeDbList: List<AnimeDbDomain> ->
                    dispatch(
                        DatabaseStore.Message.UpdateAnimeDatabaseItems(animeDbList)
                    )
                }
        }
    }

    private fun insertAnimeDatabaseItem(
        intent: DatabaseStore.Intent.InsertAnimeDatabaseItem
    ) {
        if (insertAnimeDatabaseItemsJobMap[intent.animeDatabaseItem.id]?.isActive == true) return
        if (databaseContainsItem(intent.animeDatabaseItem.id)) return
        insertAnimeDatabaseItemsJobMap[intent.animeDatabaseItem.id] =
            scope.launch(coroutineContextProvider.mainCoroutineContext) {
                usecases.insertDatabaseItemUsecase.execute(intent.animeDatabaseItem)
            }
    }

    private fun deleteAnimeDatabaseItem(intent: DatabaseStore.Intent.DeleteAnimeDatabaseItem) {
        if (deleteAnimeDatabaseItemsJobMap[intent.id]?.isActive == true) return
        if (!databaseContainsItem(intent.id)) return
        deleteAnimeDatabaseItemsJobMap[intent.id] =
            scope.launch(coroutineContextProvider.mainCoroutineContext) {
                usecases.deleteDatabaseItemUsecase.execute(intent.id)
            }
    }

    private fun resetAllItemsNewEpisodeStatus() {
        if (resetAllItemsNewEpisodeStatusJob?.isActive == true) return
        resetAllItemsNewEpisodeStatusJob =
            scope.launch(coroutineContextProvider.mainCoroutineContext) {
                usecases.resetAllDatabaseItemsNewEpisodeStatusUsecase.execute()
                publish(DatabaseStore.Label.ResetAllItemsNewEpisodeStatusWasFinished)
            }
    }

    private fun changeItemNewEpisodeStatus(
        intent: DatabaseStore.Intent.ChangeItemNewEpisodeStatus
    ) {
        if (changeItemNewEpisodeStatusJobMap[intent.id]?.isActive == true) return

        val isItemAlreadyWithoutNewEpisodeLabel = state().animeDatabaseItems
            .find { animeDb: AnimeDbDomain ->
                animeDb.id == intent.id
            }?.isNewEpisode ?: false == false

        if (isItemAlreadyWithoutNewEpisodeLabel) return

        changeItemNewEpisodeStatusJobMap[intent.id] =
            scope.launch(coroutineContextProvider.mainCoroutineContext) {
                usecases.changeDatabaseItemNewEpisodeStatusUsecase.execute(
                    id = intent.id,
                    isNewEpisode = intent.isNewEpisode
                )
            }
    }

    private fun updateAnimeDatabaseItem(intent: DatabaseStore.Intent.UpdateAnimeDatabaseItem) {
        if (updateItemJobMap[intent.animeDatabaseItem.id]?.isActive == true) return
        if (!databaseContainsItem(intent.animeDatabaseItem.id)) return
        updateItemJobMap[intent.animeDatabaseItem.id] =
            scope.launch(coroutineContextProvider.mainCoroutineContext) {
                usecases.updateDatabaseItemUsecase.execute(intent.animeDatabaseItem)
            }
    }

    private fun databaseContainsItem(id: AnimeId): Boolean {
        return state().animeDatabaseItems.map { animeDb: AnimeDbDomain ->
            animeDb.id
        }.toSet().contains(id)
    }
}
