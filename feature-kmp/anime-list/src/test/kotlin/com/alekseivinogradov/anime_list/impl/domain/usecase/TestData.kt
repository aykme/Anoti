package com.alekseivinogradov.anime_list.impl.domain.usecase

import com.alekseivinogradov.anime_base.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlin.random.Random

private val testError = Throwable()

internal fun createRandomId() = Random.nextInt(Int.MAX_VALUE)

internal fun createTestSuccessResult(id: AnimeId): CallResult.Success<ListItemDomain> {
    return CallResult.Success<ListItemDomain>(
        value = createListItem(id)
    )
}

internal fun createTestListSuccessResult(
    itemNumber: Int
): CallResult.Success<List<ListItemDomain>> {
    val list = mutableListOf<ListItemDomain>().apply {
        repeat(itemNumber) { repeatNumber: Int ->
            add(createListItem(id = repeatNumber))
        }
    }
    return CallResult.Success<List<ListItemDomain>>(
        value = list
    )
}

internal fun createTestHttpErrorResult(): CallResult.HttpError {
    return CallResult.HttpError(
        code = 404,
        throwable = testError
    )
}

internal fun createTestOtherErrorResult(): CallResult.OtherError {
    return CallResult.OtherError(
        throwable = testError
    )
}

private fun createListItem(id: AnimeId): ListItemDomain {
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
        releaseStatus = ReleaseStatusDomain.RELEASED
    )
}
