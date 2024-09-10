package com.alekseivinogradov.database.impl.domain.store

import com.alekseivinogradov.database.api.data.model.AnimeDb
import com.alekseivinogradov.database.api.domain.store.DatabaseExecutor
import com.alekseivinogradov.database.api.domain.store.DatabaseStore
import com.alekseivinogradov.database.impl.domain.usecase.DatabaseUsecases
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class DatabaseExecutorImpl(
    private val usecases: DatabaseUsecases
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
            is DatabaseStore.Intent.InsertAnimeDatabaseItem -> {
                insertAnimeDatabaseItem(intent.animeDatabaseItem)
            }

            is DatabaseStore.Intent.DeleteAnimeDatabaseItem -> {
                deleteAnimeDatabaseItem(intent.id)
            }
        }
    }

    private fun subscribeToDatabase() {
        if (fetchAllDatabaseItemsJob?.isActive == true) return
        fetchAllDatabaseItemsJob = scope.launch {
            usecases.fetchAllAnimeDatabaseItemsFlowUsecase.execute()
                .collect { animeDbList: List<AnimeDb> ->
                    dispatch(
                        DatabaseStore.Message.UpdateAnimeDatabaseItems(animeDbList)
                    )
                }
        }
    }

    private fun insertAnimeDatabaseItem(animeDatabaseItem: AnimeDb) {
        if (insertAnimeDatabaseItemsJobMap[animeDatabaseItem.id]?.isActive == true) return
        insertAnimeDatabaseItemsJobMap[animeDatabaseItem.id] = scope.launch {
            usecases.insertAnimeDatabaseItemUsecase.execute(animeDatabaseItem)
        }
    }

    private fun deleteAnimeDatabaseItem(id: Int) {
        if (deleteAnimeDatabaseItemsJobMap[id]?.isActive == true) return
        deleteAnimeDatabaseItemsJobMap[id] = scope.launch {
            usecases.deleteAnimeDatabaseItemUsecase.execute(id)
        }
    }
}