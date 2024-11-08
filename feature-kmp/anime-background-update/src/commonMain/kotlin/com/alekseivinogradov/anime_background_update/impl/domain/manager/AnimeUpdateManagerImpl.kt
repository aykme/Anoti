package com.alekseivinogradov.anime_background_update.impl.domain.manager

import com.alekseivinogradov.anime_background_update.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anime_background_update.api.domain.mapper.mapReleaseStatusDomainToDb
import com.alekseivinogradov.anime_background_update.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_background_update.api.domain.model.WorkResult
import com.alekseivinogradov.anime_background_update.impl.domain.usecase.FetchAnimeListByIdsUsecase
import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.celebrity.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.celebrity.api.domain.Index
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.database.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.database.api.domain.usecase.FetchAllDatabaseItemsUsecase
import com.alekseivinogradov.database.api.domain.usecase.UpdateDatabaseItemUsecase
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.withContext

class AnimeUpdateManagerImpl(
    private val coroutineContextProvider: CoroutineContextProvider,
    private val fetchAllDatabaseItemsUsecase: FetchAllDatabaseItemsUsecase,
    private val fetchAnimeListByIdsUsecase: FetchAnimeListByIdsUsecase,
    private val updateDatabaseItemUsecase: UpdateDatabaseItemUsecase
) : AnimeUpdateManager {

    override suspend fun update(): WorkResult {
        return withContext(coroutineContextProvider.workManagerCoroutineContext) {
            try {
                val databaseItems: List<AnimeDbDomain> = fetchAllDatabaseItemsUsecase.execute()
                val remoteItemsWithResult: Map<Index, CallResult<List<ListItemDomain>>> =
                    getRemoteItemsWithResultBySplittedRequests(databaseItems)

                updateAnimeWithWorkResult(
                    currentDatabaseItems = databaseItems,
                    remoteItemsWithResult = remoteItemsWithResult
                )
            } catch (e: Exception) {
                WorkResult.Error
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
    ): Map<Index, CallResult<List<ListItemDomain>>> =
        withContext(coroutineContextProvider.ioDispacher) {
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

            return@withContext remoteItemsWithResultIndexed.toMap()
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
     * then the result will be "WorkResult.Success".
     * If at least 1 Api request was unsuccessful,
     * the result "WorkResult.Error" will be returned.
     * In this case, the database will be updated, using only successful remote data,
     * another will be updated next time.
     * @see WorkResult - result from worker.
     */
    private suspend fun updateAnimeWithWorkResult(
        currentDatabaseItems: List<AnimeDbDomain>,
        remoteItemsWithResult: Map<Index, CallResult<List<ListItemDomain>>>
    ): WorkResult {
        val flattenedRemoteItems: MutableList<ListItemDomain> = mutableListOf()
        var isLeastOneError = false

        remoteItemsWithResult.values.forEach { remoteResult: CallResult<List<ListItemDomain>> ->
            when (remoteResult) {
                is CallResult.Success -> {
                    flattenedRemoteItems.addAll(remoteResult.value)
                }

                is CallResult.HttpError,
                is CallResult.OtherError -> {
                    isLeastOneError = true
                }
            }
        }

        val result = if (isLeastOneError.not()) {
            WorkResult.Success
        } else {
            WorkResult.Error
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
            updateDatabaseItemUsecase.execute(animeDb)
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
}
