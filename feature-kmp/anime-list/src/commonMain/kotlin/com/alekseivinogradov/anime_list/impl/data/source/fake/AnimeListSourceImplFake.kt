package com.alekseivinogradov.anime_list.impl.data.source.fake

import com.alekseivinogradov.anime_base.api.data.model.SortData
import com.alekseivinogradov.anime_base.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.source.AnimeListSource
import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.network.api.domain.model.CallResult
import com.alekseivinogradov.network.api.domain.model.test.DesiredCallResult
import kotlinx.coroutines.delay
import kotlin.time.Duration

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
        return CallResult.Success<ListItemDomain>(
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
        return CallResult.Success<List<ListItemDomain>>(
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
            score = 8.779999732971191F,
            releaseStatus = releaseStatus
        )
    }
}
