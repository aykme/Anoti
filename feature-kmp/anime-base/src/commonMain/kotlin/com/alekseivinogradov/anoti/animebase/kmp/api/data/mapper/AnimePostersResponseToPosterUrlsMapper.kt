package com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper

import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimePosterResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimePostersResponse
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

/**
 * The poster URL of each anime in the answer, keyed by its id, so the server's order does not
 * matter. An entry without a numeric id or an HTTPS poster is left out. Its anime keeps the REST
 * image.
 *
 * @throws IllegalStateException when the answer carries no data, which GraphQL sends for a query
 * that failed as a whole.
 */
internal fun AnimePostersResponse.toPosterUrls(): Map<AnimeId, String> {
    val animes = data?.animes
        ?: error("GraphQL answered without data, with ${errors.orEmpty().size} errors")
    return animes.mapNotNull(AnimePosterResponse::toPosterUrlEntry).toMap()
}

private fun AnimePosterResponse.toPosterUrlEntry(): Pair<AnimeId, String>? {
    val animeId = id?.toIntOrNull() ?: return null
    // Neither platform allows cleartext, so an http poster would only replace a working image.
    val posterUrl = poster?.originalUrl?.takeIf { it.startsWith(SECURE_URL_PREFIX) } ?: return null
    return animeId to posterUrl
}

private const val SECURE_URL_PREFIX = "https://"
