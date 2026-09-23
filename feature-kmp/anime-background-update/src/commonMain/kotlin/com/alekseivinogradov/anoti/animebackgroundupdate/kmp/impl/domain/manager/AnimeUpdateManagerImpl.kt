package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.manager

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.mapper.mapReleaseStatusDomainToDb
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.WorkResult
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.usecase.FetchAnimeListByIdsUsecase
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.FetchAllAnimeDatabaseItemsUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.UpdateAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.animenotification.kmp.api.domain.manager.AnimeNotificationManager
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class AnimeUpdateManagerImpl(
    private val coroutineContextProvider: CoroutineContextProvider,
    private val fetchAllAnimeDatabaseItemsUsecase: FetchAllAnimeDatabaseItemsUsecase,
    private val fetchAnimeListByIdsUsecase: FetchAnimeListByIdsUsecase,
    private val updateAnimeDatabaseItemUsecase: UpdateAnimeDatabaseItemUsecase,
    private val notificationManager: AnimeNotificationManager
) : AnimeUpdateManager {

    override suspend fun update(): WorkResult {
        return withContext(coroutineContextProvider.workManagerCoroutineContext) {
            try {
                applyFreshData(fetchAllAnimeDatabaseItemsUsecase.execute())
            } catch (e: CancellationException) {
                throw e
            } catch (
                // Best-effort background update; falling back to WorkResult.Error on any other
                // failure (logged below) is this method's whole purpose.
                @Suppress("TooGenericExceptionCaught") e: Exception
            ) {
                println("AnimeUpdateManagerImpl $e")
                WorkResult.Error
            }
        }
    }

    /**
     * Walks the saved library one page at a time, applying each page as soon as it arrives. The
     * API caps how many anime a single call may return, so the rows are split to fit.
     *
     * Only one page of fetched data is held at a time, and a pass that is stopped part-way
     * keeps the pages it already applied.
     *
     * @return [WorkResult.Success] once every page arrived. A page the server did not answer
     * for gives [WorkResult.Error]; the rest is still applied and that page waits for the next
     * pass.
     */
    private suspend fun applyFreshData(databaseItems: List<AnimeDbDomain>): WorkResult =
        withContext(coroutineContextProvider.ioDispatcher) {
            var everyPageArrived = true

            databaseItems.chunked(ITEMS_PER_PAGE).forEach { page: List<AnimeDbDomain> ->
                val fetched = fetchAnimeListByIdsUsecase.execute(
                    page.joinToString(separator = ",") { item: AnimeDbDomain ->
                        item.id.toString()
                    }
                )

                when (fetched) {
                    is CallResult.Success -> applyPage(
                        currentDatabaseItems = page,
                        remoteItems = fetched.value
                    )

                    is CallResult.Failure -> everyPageArrived = false
                }
            }

            if (everyPageArrived) WorkResult.Success else WorkResult.Error
        }

    private suspend fun applyPage(
        currentDatabaseItems: List<AnimeDbDomain>,
        remoteItems: List<ListItemDomain>
    ) {
        val currentDatabaseItemsWithIds: Map<AnimeId, AnimeDbDomain> = currentDatabaseItems
            .associateBy(AnimeDbDomain::id)

        getUpdatedDatabaseItems(
            currentDatabaseItems = currentDatabaseItems,
            remoteItems = remoteItems
        ).forEach { updatedDatabaseItem: AnimeDbDomain ->
            // Notify first: once the row carries the new episode count, the next pass sees no
            // change and would never notify about it.
            currentDatabaseItemsWithIds[updatedDatabaseItem.id]
                ?.let { currentDatabaseItem: AnimeDbDomain ->
                    makeNewEpisodeNotificationIfNecessary(
                        currentDatabaseItem = currentDatabaseItem,
                        updatedDatabaseItem = updatedDatabaseItem
                    )
                }
            updateAnimeDatabaseItemUsecase.execute(updatedDatabaseItem)
        }
    }

    /** The rows whose fresh data differs from what is saved. An unchanged row is left out. */
    private fun getUpdatedDatabaseItems(
        currentDatabaseItems: List<AnimeDbDomain>,
        remoteItems: List<ListItemDomain>
    ): List<AnimeDbDomain> {
        // Keyed by id to avoid a nested scan over the page.
        val remoteItemsWithIds: Map<AnimeId, ListItemDomain> = remoteItems
            .associateBy(ListItemDomain::id)

        return currentDatabaseItems.mapNotNull { animeDb: AnimeDbDomain ->
            val remoteItem = remoteItemsWithIds[animeDb.id] ?: return@mapNotNull null
            val updatedDatabaseItem = animeDb.copy(
                imageUrl = remoteItem.imageUrl,
                name = remoteItem.name,
                episodesAired = remoteItem.episodesAired,
                episodesTotal = remoteItem.episodesTotal,
                airedOn = remoteItem.airedOn,
                releasedOn = remoteItem.releasedOn,
                score = remoteItem.score,
                releaseStatus = mapReleaseStatusDomainToDb(remoteItem.releaseStatus),
                isNewEpisode = isNewEpisodeDbStatus(
                    currentDatabaseItem = animeDb,
                    remoteItem = remoteItem
                )
            )

            updatedDatabaseItem.takeIf { it != animeDb }
        }
    }

    private fun isNewEpisodeDbStatus(
        currentDatabaseItem: AnimeDbDomain,
        remoteItem: ListItemDomain
    ): Boolean {
        val currentEpisodesAired = currentDatabaseItem.episodesAired ?: 0
        val newEpisodesAired = remoteItem.episodesAired ?: 0

        return currentDatabaseItem.isNewEpisode ||
            (
                currentDatabaseItem.releaseStatus != ReleaseStatusDb.RELEASED &&
                    remoteItem.releaseStatus == ReleaseStatusDomain.RELEASED
                ) ||
            newEpisodesAired > currentEpisodesAired
    }

    private suspend fun makeNewEpisodeNotificationIfNecessary(
        currentDatabaseItem: AnimeDbDomain,
        updatedDatabaseItem: AnimeDbDomain
    ) {
        if (
            isNotificationNecessary(
                currentDatabaseItem = currentDatabaseItem,
                updatedDatabaseItem = updatedDatabaseItem
            )
        ) {
            val airedEpisode = getAiredEpisode(updatedDatabaseItem)
            notificationManager.makeNewEpisodeNotification(
                animeName = updatedDatabaseItem.name,
                airedEpisode = airedEpisode,
                imageUrl = updatedDatabaseItem.imageUrl
            )
        }
    }

    private fun isNotificationNecessary(
        currentDatabaseItem: AnimeDbDomain,
        updatedDatabaseItem: AnimeDbDomain
    ): Boolean {
        if (
            currentDatabaseItem.releaseStatus != ReleaseStatusDb.RELEASED &&
            updatedDatabaseItem.releaseStatus == ReleaseStatusDb.RELEASED
        ) {
            return true
        }
        val currentEpisodesAired = currentDatabaseItem.episodesAired ?: 0
        val newEpisodesAired = updatedDatabaseItem.episodesAired ?: 0
        return newEpisodesAired > currentEpisodesAired
    }

    private fun getAiredEpisode(item: AnimeDbDomain): Int {
        val episodesAired = item.episodesAired ?: 0
        return if (item.releaseStatus == ReleaseStatusDb.RELEASED) {
            item.episodesTotal ?: episodesAired
        } else {
            episodesAired
        }
    }
}
