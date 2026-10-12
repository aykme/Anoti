package com.alekseivinogradov.anoti.animebase.kmp.impl.data.source

import com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper.toPosterUrls
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.source.AnimePosterSource
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.ANOTI_TAG
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

class AnimePosterSourceImpl(
    private val service: ShikimoriApiService,
    private val safeApi: SafeApi
) : AnimePosterSource {

    override suspend fun getPosterUrls(ids: List<AnimeId>): Map<AnimeId, String> {
        if (ids.isEmpty()) return emptyMap()

        return when (val result = safeApi.call { service.getAnimePosters(ids).toPosterUrls() }) {
            is CallResult.Success -> result.value.also { posterUrls: Map<AnimeId, String> ->
                println("$ANOTI_TAG $TAG: ${posterUrls.size} posters for ${ids.size} ids")
            }

            is CallResult.Failure -> emptyMap<AnimeId, String>().also {
                println(
                    "$ANOTI_TAG $TAG: the poster lookup failed (${describeFailure(result)}), " +
                        "REST images stay"
                )
            }
        }
    }
}

/** The kind of [failure], for a log line: never its message, which may quote the server. */
internal fun describeFailure(failure: CallResult.Failure): String = when (failure) {
    is CallResult.HttpError -> "HTTP ${failure.code}"
    is CallResult.NetworkError -> "network error"
    is CallResult.OtherError -> "other error"
}

private const val TAG = "AnimePosterSource"
