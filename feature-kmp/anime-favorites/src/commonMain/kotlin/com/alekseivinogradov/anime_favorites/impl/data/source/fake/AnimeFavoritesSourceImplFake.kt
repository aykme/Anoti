package com.alekseivinogradov.anime_favorites.impl.data.source.fake

import com.alekseivinogradov.anime_base.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anime_favorites.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_favorites.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.network.api.domain.model.CallResult
import com.alekseivinogradov.network.api.domain.model.test.DesiredCallResult
import kotlinx.coroutines.delay
import kotlin.time.Duration

class AnimeFavoritesSourceImplFake(
    private val desiredCallResult: DesiredCallResult,
    private val desiredDelay: Duration
) : AnimeFavoritesSource {

    private val testError = Throwable()

    override suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain> {
        delay(desiredDelay)
        return when (desiredCallResult) {
            DesiredCallResult.SUCCESS -> createTestSuccessResult(id)
            DesiredCallResult.HTTP_ERROR -> createTestHttpErrorResult()
            DesiredCallResult.OTHER_ERROR -> createTestOtherErrorResult()
        }
    }

    private fun createTestSuccessResult(id: AnimeId): CallResult.Success<ListItemDomain> {
        return CallResult.Success<ListItemDomain>(
            ListItemDomain(
                id = id,
                name = "Shingeki no Kyojin: The Final Season",
                imageUrl = "https://shikimori.one/system/animes/original/40028.jpg?1711973445",
                episodesAired = 16,
                episodesTotal = 16,
                nextEpisodeAt = null,
                airedOn = "2020-12-07",
                releasedOn = "2021-03-29",
                score = 8.779999732971191F,
                releaseStatus = ReleaseStatusDomain.RELEASED,
                episodesViewed = 0,
                isNewEpisode = false
            )
        )
    }

    private fun createTestHttpErrorResult(): CallResult.HttpError {
        return CallResult.HttpError(
            code = 404,
            throwable = testError
        )
    }

    private fun createTestOtherErrorResult(): CallResult.OtherError {
        return CallResult.OtherError(
            throwable = testError
        )
    }
}
