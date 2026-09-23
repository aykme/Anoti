package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.impl.data.source.fake.AnimeListSourceFake
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
        releaseStatus = ReleaseStatusDomain.ONGOING
    )

    @Test
    fun theIdItIsCalledWithIsTheIdTheSourceIsAskedFor() = runTest {
        //Given
        val source = AnimeListSourceFake(byId = { CallResult.Success(item) })

        //When
        FetchAnimeDetailsByIdUsecase(source).execute(ANIME_ID)

        //Then
        assertEquals(listOf(ANIME_ID), source.requestedIds)
    }

    @Test
    fun theItemTheSourceAnswersWithIsHandedBackUnchanged() = runTest {
        //Given
        val source = AnimeListSourceFake(byId = { CallResult.Success(item) })

        //When
        val result = FetchAnimeDetailsByIdUsecase(source).execute(ANIME_ID)

        //Then
        assertEquals(CallResult.Success(item), result)
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
                AnimeListSourceFake(byId = { failure })
            ).execute(ANIME_ID)
        }

        //Then
        assertEquals(failures, results)
    }
}
