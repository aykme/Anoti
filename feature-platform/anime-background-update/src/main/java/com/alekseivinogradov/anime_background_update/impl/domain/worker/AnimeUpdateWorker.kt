package com.alekseivinogradov.anime_background_update.impl.domain.worker

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.alekseivinogradov.anime_background_update.api.domain.mapper.mapReleaseStatusDomainToDb
import com.alekseivinogradov.anime_background_update.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_background_update.impl.domain.usecase.FetchAnimeListByIdsUsecase
import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.celebrity.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.celebrity.api.domain.Index
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.database.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.withContext

@WorkerThread
class AnimeUpdateWorker(
    appContext: Context,
    params: WorkerParameters,
    private val coroutineContextProvider: CoroutineContextProvider,
    private val animeDatabaseRepository: AnimeDatabaseRepository,
    private val fetchAnimeListByIdsUsecase: FetchAnimeListByIdsUsecase
) : CoroutineWorker(appContext, params) {

    companion object {
        const val animeUpdateWorkName = "ANIME_UPDATE_WORK"
    }

    override suspend fun doWork(): Result {
        return withContext(coroutineContextProvider.workManagerCoroutineContext) {
            try {
                val databaseItems: List<AnimeDbDomain> = animeDatabaseRepository.getAllItems()
                val remoteItemsWithResult: Map<Index, CallResult<List<ListItemDomain>>> =
                    getRemoteItemsWithResultBySplittedRequests(databaseItems)

                updateAnimeWithWorkResult(
                    currentDatabaseItems = databaseItems,
                    remoteItemsWithResult = remoteItemsWithResult
                )
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }

    /**
     * Get remote items with result by splitted requests.
     * @param databaseItems - database items.
     * The Api used has a limit on the number of items to be returned,
     * so requests must be splitted.
     * @return - map of results with indexes.
     * @see CallResult - result from Api methods.
     */
    private suspend fun getRemoteItemsWithResultBySplittedRequests(
        databaseItems: List<AnimeDbDomain>
    ): Map<Index, CallResult<List<ListItemDomain>>> {
        val remoteItemsWithResultIndexed: MutableMap<Index, CallResult<List<ListItemDomain>>> =
            mutableMapOf()
        var requestIndex = 0

        val remainingRemoteItemIdsForFetching = databaseItems
            .map { animeDb: AnimeDbDomain ->
                animeDb.id
            }.toMutableSet()

        while (remainingRemoteItemIdsForFetching.size > 0) {
            val currentRemouteItemIdsForFetching = remainingRemoteItemIdsForFetching
                .take(ITEMS_PER_PAGE).toSet()

            val remoteItemsWithResult = getRemoteItemsWithResult(
                itemIds = getItemIdsString(items = currentRemouteItemIdsForFetching)
            )

            remoteItemsWithResultIndexed[requestIndex] = remoteItemsWithResult
            requestIndex++
            remainingRemoteItemIdsForFetching.removeAll(currentRemouteItemIdsForFetching)
        }

        return remoteItemsWithResultIndexed.toMap()
    }

    private fun getItemIdsString(items: Set<AnimeId>): String {
        return items.joinToString(separator = ",")
    }

    private suspend fun getRemoteItemsWithResult(
        itemIds: String
    ): CallResult<List<ListItemDomain>> {
        return fetchAnimeListByIdsUsecase.execute(itemIds)
    }

    /**
     * Update database with work result.
     * @param currentDatabaseItems - current database items.
     * @param remoteItemsWithResult - remote items with result.
     * @see CallResult - result from Api methods.
     * @return - If all the results of the Api request are successful,
     * then the result will be "ListenableWorker.Result.success()".
     * If at least 1 Api request was unsuccessful,
     * the result "ListenableWorker.Result.retry()" will be returned.
     * In this case, the database will be updated, using only successful results,
     * but worker will be restarted in the near future and it try again.
     * @see ListenableWorker.Result - result from worker.
     */
    private suspend fun updateAnimeWithWorkResult(
        currentDatabaseItems: List<AnimeDbDomain>,
        remoteItemsWithResult: Map<Index, CallResult<List<ListItemDomain>>>
    ): ListenableWorker.Result {
        var result = ListenableWorker.Result.success()
        val flattenedRemoteItems: MutableList<ListItemDomain> = mutableListOf()

        remoteItemsWithResult.values.forEach { remoteResult: CallResult<List<ListItemDomain>> ->
            when (remoteResult) {
                is CallResult.Success -> {
                    flattenedRemoteItems.addAll(remoteResult.value)
                }

                is CallResult.HttpError,
                is CallResult.OtherError -> {
                    result = ListenableWorker.Result.retry()
                }
            }
        }

        updateAnime(
            currentDatabaseItems = currentDatabaseItems,
            remoteItems = flattenedRemoteItems.toList()
        )

        return result
    }

    private suspend fun updateAnime(
        currentDatabaseItems: List<AnimeDbDomain>,
        remoteItems: List<ListItemDomain>
    ) {
        val updatedDatabaseItems = getUpdatedDatabaseItems(
            currentDatabaseItems = currentDatabaseItems,
            remoteItems = remoteItems
        )

        updatedDatabaseItems.forEach { animeDb: AnimeDbDomain ->
            animeDatabaseRepository.update(animeDb)
        }
    }

    private fun getUpdatedDatabaseItems(
        currentDatabaseItems: List<AnimeDbDomain>,
        remoteItems: List<ListItemDomain>
    ): List<AnimeDbDomain> {
        /**
         * Transform List into Map to avoid nested iteration, using indexes for quick access
         */
        val remoteItemsWithIds: Map<AnimeId, ListItemDomain> = remoteItems
            .associateBy { itemDomain: ListItemDomain ->
                itemDomain.id
            }

        val updatedDatabaseItems = currentDatabaseItems.map { animeDb: AnimeDbDomain ->
            val remoteItem = remoteItemsWithIds[animeDb.id]
            remoteItem?.let { remoteItemNotNull: ListItemDomain ->
                val updatedDatabaseItem = animeDb.copy(
                    imageUrl = remoteItemNotNull.imageUrl,
                    name = remoteItemNotNull.name,
                    episodesAired = remoteItemNotNull.episodesAired,
                    episodesTotal = remoteItemNotNull.episodesTotal,
                    airedOn = remoteItemNotNull.airedOn,
                    releasedOn = remoteItemNotNull.releasedOn,
                    score = remoteItemNotNull.score,
                    releaseStatus = mapReleaseStatusDomainToDb(remoteItemNotNull.releaseStatus),
                    isNewEpisode = isNewEpisode(
                        currentDatabaseItem = animeDb,
                        remoteItem = remoteItemNotNull
                    )
                )

                if (updatedDatabaseItem != animeDb) {
                    updatedDatabaseItem
                } else null
            }
        }.filterNotNull()

        return updatedDatabaseItems
    }

    private fun isNewEpisode(
        currentDatabaseItem: AnimeDbDomain,
        remoteItem: ListItemDomain
    ): Boolean {
        val currentEpisodesAired = currentDatabaseItem.episodesAired ?: 0
        val newEpisodesAired = remoteItem.episodesAired ?: 0
        return newEpisodesAired > currentEpisodesAired
    }

    class Factory(
        private val coroutineContextProvider: CoroutineContextProvider,
        private val animeDatabaseRepository: AnimeDatabaseRepository,
        private val fetchAnimeListByIdsUsecase: FetchAnimeListByIdsUsecase
    ) : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker {
            return AnimeUpdateWorker(
                appContext,
                workerParameters,
                coroutineContextProvider,
                animeDatabaseRepository,
                fetchAnimeListByIdsUsecase
            )
        }
    }
}
