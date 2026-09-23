package com.alekseivinogradov.anoti.animefavorites.kmp.impl.data.source.fake

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.fake.CallResultFake
import kotlinx.coroutines.delay
import kotlin.time.Duration

class AnimeFavoritesSourceFake(
    private val callResultFake: CallResultFake,
    private val desiredDelay: Duration
) : AnimeFavoritesSource {

    private val error = Throwable("Simulated failure from AnimeFavoritesSourceFake")

    override suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain> {
        delay(desiredDelay)
        return when (callResultFake) {
            CallResultFake.SUCCESS -> createAnimeDetailsSuccessResult(id)
            CallResultFake.HTTP_ERROR -> createHttpErrorResult()
            CallResultFake.OTHER_ERROR -> createOtherErrorResult()
        }
    }

    private fun createAnimeDetailsSuccessResult(id: AnimeId): CallResult.Success<ListItemDomain> {
        return CallResult.Success(
            ListItemDomain(
                id = id,
                name = "Shingeki no Kyojin: The Final Season",
                imageUrl = "https://shikimori.io/system/animes/original/40028.jpg?1711973445",
                episodesAired = 16,
                episodesTotal = 16,
                nextEpisodeAt = "2020-19-07",
                airedOn = "2020-12-07",
                releasedOn = "2021-03-29",
                score = 8.78F,
                releaseStatus = ReleaseStatusDomain.RELEASED,
                episodesViewed = 0,
                isNewEpisode = false
            )
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
}
