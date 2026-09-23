package com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase.fake

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.ChangeAnimeDatabaseItemNewEpisodeStatusUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.DeleteAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.FetchAllAnimeDatabaseItemsFlowUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.FetchAllAnimeDatabaseItemsUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.InsertAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.ResetAllAnimeDatabaseItemsExtraInfoUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.UpdateAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.wrapper.AnimeDatabaseUsecases
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * The whole set of saved-anime usecases, standing in for a database. Reads come from [items],
 * which the test sets. Write is only recorded, never applied to [items], so a test asserts
 * what the code under test asked for rather than what a database would hold afterward.
 *
 * @param initialItems the saved anime the fake starts with.
 */
class AnimeDatabaseUsecasesFake(initialItems: List<AnimeDbDomain> = listOf()) {

    /** The saved anime the fetch usecase emits. Set it to drive the code under test. */
    val items = MutableStateFlow(initialItems)

    /** Items passed to the insert usecase, oldest first. */
    val insertedItems: List<AnimeDbDomain>
        field = mutableListOf<AnimeDbDomain>()

    /** Ids passed to delete usecase, oldest first. */
    val deletedIds: List<AnimeId>
        field = mutableListOf<AnimeId>()

    /** Items passed to the update usecase, oldest first. */
    val updatedItems: List<AnimeDbDomain>
        field = mutableListOf<AnimeDbDomain>()

    /** ID and requested flag of every new-episode status change, oldest first. */
    val newEpisodeStatusChanges: List<Pair<AnimeId, Boolean>>
        field = mutableListOf<Pair<AnimeId, Boolean>>()

    /** How many times the whole library was asked to drop its new-episode marks. */
    var resetNewEpisodeStatusCount = 0
        private set

    /** How many times the whole library was asked to drop its extra episode info. */
    var resetExtraInfoCount = 0
        private set

    /**
     * The one-shot read of the whole library. Answers with whatever [items] currently holds.
     * It sits outside [usecases] because [AnimeDatabaseUsecases] does not carry it.
     */
    val fetchAllAnimeDatabaseItemsUsecase = object : FetchAllAnimeDatabaseItemsUsecase {
        override suspend fun execute(): List<AnimeDbDomain> = items.value
    }

    /** The set to hand to the code under test. */
    val usecases = AnimeDatabaseUsecases(
        fetchAllAnimeDatabaseItemsFlowUsecase = object : FetchAllAnimeDatabaseItemsFlowUsecase {
            override fun execute(): Flow<List<AnimeDbDomain>> = items
        },
        insertAnimeDatabaseItemUsecase = object : InsertAnimeDatabaseItemUsecase {
            override suspend fun execute(anime: AnimeDbDomain) {
                insertedItems += anime
            }
        },
        deleteAnimeDatabaseItemUsecase = object : DeleteAnimeDatabaseItemUsecase {
            override suspend fun execute(id: AnimeId) {
                deletedIds += id
            }
        },
        resetAllAnimeDatabaseItemsNewEpisodeStatusUsecase =
        object : ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecase {
            override suspend fun execute() {
                resetNewEpisodeStatusCount++
            }
        },
        changeAnimeDatabaseItemNewEpisodeStatusUsecase =
        object : ChangeAnimeDatabaseItemNewEpisodeStatusUsecase {
            override suspend fun execute(id: Int, isNewEpisode: Boolean) {
                newEpisodeStatusChanges += id to isNewEpisode
            }
        },
        updateAnimeDatabaseItemUsecase = object : UpdateAnimeDatabaseItemUsecase {
            override suspend fun execute(anime: AnimeDbDomain) {
                updatedItems += anime
            }
        },
        resetAllAnimeDatabaseItemsExtraInfoUsecase =
        object : ResetAllAnimeDatabaseItemsExtraInfoUsecase {
            override suspend fun execute() {
                resetExtraInfoCount++
            }
        }
    )
}
