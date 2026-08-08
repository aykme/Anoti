package com.alekseivinogradov.anoti.animelist.kmp.impl.data.source.fake

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.test.DesiredCallResult
import kotlin.time.Duration
import kotlinx.coroutines.delay

class AnimeListSourceImplFake(
    private val desiredCallResult: DesiredCallResult,
    private val desiredDelay: Duration
) : AnimeListSource {

    private val error = Throwable()

    override suspend fun getOngoingList(
        page: Int,
        sort: SortData
    ): CallResult<List<ListItemDomain>> {
        delay(desiredDelay)
        return when (desiredCallResult) {
            DesiredCallResult.SUCCESS -> createAnimeListSuccessResult(
                itemNumber = 5,
                releaseStatus = ReleaseStatusDomain.ONGOING
            )

            DesiredCallResult.HTTP_ERROR -> createHttpErrorResult()
            DesiredCallResult.OTHER_ERROR -> createOtherErrorResult()
        }
    }

    override suspend fun getAnnouncedList(
        page: Int,
        sort: SortData
    ): CallResult<List<ListItemDomain>> {
        delay(desiredDelay)
        return when (desiredCallResult) {
            DesiredCallResult.SUCCESS -> createAnimeListSuccessResult(
                itemNumber = 5,
                releaseStatus = ReleaseStatusDomain.ANNOUNCED
            )

            DesiredCallResult.HTTP_ERROR -> createHttpErrorResult()
            DesiredCallResult.OTHER_ERROR -> createOtherErrorResult()
        }
    }

    override suspend fun getListBySearch(
        page: Int,
        search: String,
        sort: SortData
    ): CallResult<List<ListItemDomain>> {
        delay(desiredDelay)
        return when (desiredCallResult) {
            DesiredCallResult.SUCCESS -> createAnimeListSuccessResult(itemNumber = 5)
            DesiredCallResult.HTTP_ERROR -> createHttpErrorResult()
            DesiredCallResult.OTHER_ERROR -> createOtherErrorResult()
        }
    }

    override suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain> {
        delay(desiredDelay)
        return when (desiredCallResult) {
            DesiredCallResult.SUCCESS -> createAnimeDetailsSuccessResult(id)
            DesiredCallResult.HTTP_ERROR -> createHttpErrorResult()
            DesiredCallResult.OTHER_ERROR -> createOtherErrorResult()
        }
    }

    private fun createAnimeDetailsSuccessResult(
        id: AnimeId,
        releaseStatus: ReleaseStatusDomain = ReleaseStatusDomain.RELEASED
    ): CallResult.Success<ListItemDomain> {
        return CallResult.Success(
            value = createAnimeItem(
                id = id,
                releaseStatus = releaseStatus
            )
        )
    }

    private fun createAnimeListSuccessResult(
        itemNumber: Int,
        releaseStatus: ReleaseStatusDomain = ReleaseStatusDomain.RELEASED
    ): CallResult.Success<List<ListItemDomain>> {
        val list = mutableListOf<ListItemDomain>().apply {
            repeat(itemNumber) { repeatNumber: Int ->
                add(createAnimeItem(id = repeatNumber, releaseStatus = releaseStatus))
            }
        }
        return CallResult.Success(
            value = list.toList()
        )
    }

    private fun createHttpErrorResult(): CallResult.HttpError {
        return CallResult.HttpError(
            code = 404,
            throwable = error
        )
    }

    private fun createOtherErrorResult(): CallResult.OtherError {
        return CallResult.OtherError(
            throwable = error
        )
    }

    private fun createAnimeItem(
        id: AnimeId,
        releaseStatus: ReleaseStatusDomain
    ): ListItemDomain {
        return ListItemDomain(
            id = id,
            name = "Shingeki no Kyojin: The Final Season",
            imageUrl = "https://shikimori.one/system/animes/original/40028.jpg?1711973445",
            episodesAired = 16,
            episodesTotal = 16,
            nextEpisodeAt = null,
            airedOn = "2020-12-07",
            releasedOn = "2021-03-29",
            score = 8.78F,
            releaseStatus = releaseStatus
        )
    }
}
