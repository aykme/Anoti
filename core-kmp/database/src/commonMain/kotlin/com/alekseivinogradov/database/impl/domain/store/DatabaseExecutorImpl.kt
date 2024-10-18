package com.alekseivinogradov.database.impl.domain.store

import com.alekseivinogradov.database.api.data.model.AnimeDb
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.api.domain.store.DatabaseExecutor
import com.alekseivinogradov.database.api.domain.store.DatabaseStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class DatabaseExecutorImpl(
    private val repository: AnimeDatabaseRepository
) : DatabaseExecutor() {

    private var fetchAllDatabaseItemsJob: Job? = null
    private val insertAnimeDatabaseItemsJobMap = mutableMapOf<Int, Job>()
    private val deleteAnimeDatabaseItemsJobMap = mutableMapOf<Int, Job>()

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
        }
    }

    private fun subscribeToDatabase() {
        if (fetchAllDatabaseItemsJob?.isActive == true) return
        fetchAllDatabaseItemsJob = scope.launch {
            repository.getAllItemsFlow()
                .collect { animeDbList: List<AnimeDb> ->
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
        insertAnimeDatabaseItemsJobMap[intent.animeDatabaseItem.id] = scope.launch {
            repository.insert(intent.animeDatabaseItem)
        }
    }

    private fun deleteAnimeDatabaseItem(intent: DatabaseStore.Intent.DeleteAnimeDatabaseItem) {
        if (deleteAnimeDatabaseItemsJobMap[intent.id]?.isActive == true) return
        deleteAnimeDatabaseItemsJobMap[intent.id] = scope.launch {
            repository.delete(intent.id)
        }
    }

    private fun resetAllItemsNewEpisodeStatus() {

    }
}
