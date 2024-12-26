package com.alekseivinogradov.anime_favorites.impl.data.source

import com.alekseivinogradov.anime_base.api.data.response.AnimeDetailsResponse
import com.alekseivinogradov.anime_base.api.data.response.AnimeShortResponse
import com.alekseivinogradov.anime_base.api.data.response.ImageResponse
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.celebrity.api.domain.AnimeId
import kotlinx.coroutines.delay
import kotlin.time.Duration

class ShikimoriApiServiceImplFake(
    private val desiredCallResult: DesiredCallResult,
    private val desiredDelay: Duration
) : ShikimoriApiService {

    private val testError = Throwable()

    override suspend fun getAnimeList(
        page: Int,
        releaseStatus: String?,
        sort: String?,
        search: String?,
        ids: String?
    ): List<AnimeShortResponse> {
        throw NotImplementedError()
    }

    override suspend fun getAnimeById(id: AnimeId): AnimeDetailsResponse {
        delay(desiredDelay)
        return when (desiredCallResult) {
            DesiredCallResult.SUCCESS -> createTestResponse(id)
            else -> throw testError
        }
    }

    private fun createTestResponse(id: AnimeId): AnimeDetailsResponse {
        return AnimeDetailsResponse(
            id = id,
            englishName = "Bleach: Sennen Kessen-hen - Soukoku-tan",
            russianName = "Блич: Тысячелетняя кровавая война — Конфликт",
            pageUrl = "/animes/56784-bleach-sennen-kessen-hen-soukoku-tan",
            imageResponse = createImageResponse(),
            episodesAired = 12,
            episodesTotal = 14,
            nextEpisodeAt = "2024-12-28T17:00:00.000+03:00",
            airedOn = "2024-10-05",
            releasedOn = "2024-12-28",
            score = 8.97F,
            releaseStatus = "ongoing",
            kind = "tv",
            description = "Борьба с «Ванденрейхом» не прекращается ни на минуту, Сообщество душ всеми силами пытается остановить врага, но [character=68537]Яхве[/character] всё же удаётся заполучить неимоверную силу, пробравшись во владения Короля душ. Теперь [character=68537]Яхве[/character] стал намного сильнее, а шансы на победу синигами приблизились к нулю."
        )
    }

    private fun createImageResponse(): ImageResponse {
        return ImageResponse(
            originalSizeUrl = "/system/animes/original/56784.jpg?1711828335",
            previewSizeUrl = "/system/animes/preview/56784.jpg?1711828335",
            x96SizeUrl = "/system/animes/x96/56784.jpg?1711828335",
            x48SizeUrl = "/system/animes/x48/56784.jpg?1711828335"
        )
    }
}
