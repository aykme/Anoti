package com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.data.source.fake.AnimeFavoritesSourceFake
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val ANIME_ID = 61316
private const val HTTP_ERROR_CODE = 500

class FetchAnimeDetailsByIdUsecaseTest {

    private val item = ListItemDomain(
        id = ANIME_ID,
        name = "Frieren",
        imageUrl = null,
        episodesAired = 7,
        episodesTotal = 28,
        nextEpisodeAt = null,
        airedOn = "2025-10-01",
        releasedOn = null,
        score = 9.11F,
        releaseStatus = ReleaseStatusDomain.ONGOING,
        episodesViewed = 0,
        isNewEpisode = false
    )

    @Test
    fun theItemTheSourceAnswersWithIsHandedBackUnchanged() = runTest {
        //Given
        val usecase = FetchAnimeDetailsByIdUsecase(
            AnimeFavoritesSourceFake { _, _ -> CallResult.Success(item) }
        )

        //When
        val result = usecase.execute(ANIME_ID)

        //Then
        assertEquals(CallResult.Success(item), result)
    }

    @Test
    fun theIdItIsCalledWithIsTheIdTheSourceIsAskedFor() = runTest {
        //Given
        var askedFor: AnimeId? = null
        val usecase = FetchAnimeDetailsByIdUsecase(
            AnimeFavoritesSourceFake { id, _ ->
                askedFor = id
                CallResult.Success(item)
            }
        )

        //When
        usecase.execute(ANIME_ID)

        //Then
        assertEquals(ANIME_ID, askedFor)
    }

    @Test
    fun aFailureFromTheSourceIsHandedBackUnchanged() = runTest {
        //Given
        val failures = listOf(
            CallResult.HttpError(code = HTTP_ERROR_CODE, throwable = Throwable("server is down")),
            CallResult.NetworkError(throwable = Throwable("no route to host")),
            CallResult.OtherError(throwable = Throwable("something else"))
        )

        //When
        val results = failures.map { failure: CallResult.Failure ->
            FetchAnimeDetailsByIdUsecase(
                AnimeFavoritesSourceFake { _, _ -> failure }
            ).execute(ANIME_ID)
        }

        //Then
        assertEquals(failures, results)
    }
}
