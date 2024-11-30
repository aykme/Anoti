package com.alekseivinogradov.anime_database.impl.domain.store

import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.anime_database.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseExecutor
import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anime_database.api.domain.usecase.wrapper.AnimeDatabaseUsecases
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AnimeDatabaseExecutorImpl(
    private val coroutineContextProvider: CoroutineContextProvider,
    private val usecases: AnimeDatabaseUsecases
) : AnimeDatabaseExecutor() {

    private var fetchAllDatabaseItemsJob: Job? = null
    private val insertDatabaseItemsJobMap: MutableMap<AnimeId, Job> = mutableMapOf()
    private val deleteDatabaseItemsJobMap: MutableMap<AnimeId, Job> = mutableMapOf()
    private var resetAllItemsNewEpisodeStatusJob: Job? = null
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
        }
    }

    private fun subscribeToDatabase() {
        if (fetchAllDatabaseItemsJob?.isActive == true) return
        fetchAllDatabaseItemsJob = scope
            .launch(coroutineContextProvider.mainCoroutineContext) {
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
            scope.launch(coroutineContextProvider.mainCoroutineContext) {
                usecases.insertAnimeDatabaseItemUsecase.execute(intent.animeDatabaseItem)
            }
    }

    private fun deleteAnimeDatabaseItem(
        intent: AnimeDatabaseStore.Intent.DeleteAnimeDatabaseItem
    ) {
        if (deleteDatabaseItemsJobMap[intent.id]?.isActive == true) return
        if (!databaseContainsItem(intent.id)) return
        deleteDatabaseItemsJobMap[intent.id] =
            scope.launch(coroutineContextProvider.mainCoroutineContext) {
                usecases.deleteAnimeDatabaseItemUsecase.execute(intent.id)
            }
    }

    private fun resetAllItemsNewEpisodeStatus() {
        if (resetAllItemsNewEpisodeStatusJob?.isActive == true) return
        resetAllItemsNewEpisodeStatusJob =
            scope.launch(coroutineContextProvider.mainCoroutineContext) {
                usecases.resetAllAnimeDatabaseItemsNewEpisodeStatusUsecase.execute()
                publish(AnimeDatabaseStore.Label.ResetAllItemsNewEpisodeStatusWasFinished)
            }
    }

    private fun changeItemNewEpisodeStatus(
        intent: AnimeDatabaseStore.Intent.ChangeItemNewEpisodeStatus
    ) {
        if (changeItemNewEpisodeStatusJobMap[intent.id]?.isActive == true) return

        val isItemAlreadyWithoutNewEpisodeLabel = state().animeDatabaseItems
            .find { animeDb: AnimeDbDomain ->
                animeDb.id == intent.id
            }?.isNewEpisode ?: false == false

        if (isItemAlreadyWithoutNewEpisodeLabel) return

        changeItemNewEpisodeStatusJobMap[intent.id] =
            scope.launch(coroutineContextProvider.mainCoroutineContext) {
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
            scope.launch(coroutineContextProvider.mainCoroutineContext) {
                usecases.updateAnimeDatabaseItemUsecase.execute(intent.animeDatabaseItem)
            }
    }

    private fun databaseContainsItem(id: AnimeId): Boolean {
        return state().animeDatabaseItems.map { animeDb: AnimeDbDomain ->
            animeDb.id
        }.toSet().contains(id)
    }
}
