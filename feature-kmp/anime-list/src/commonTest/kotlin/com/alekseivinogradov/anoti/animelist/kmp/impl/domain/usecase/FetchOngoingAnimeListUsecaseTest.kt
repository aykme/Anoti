package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.impl.data.source.fake.AnimeListSourceFake
import com.alekseivinogradov.anoti.animelist.kmp.impl.data.source.fake.PageCall
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val PAGE = 3
private const val HTTP_ERROR_CODE = 500

class FetchOngoingAnimeListUsecaseTest {

    private val items = listOf(
        ListItemDomain(
            id = 1,
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
    )

    @Test
    fun thePageAskedForIsFetchedSortedByScore() = runTest {
        //Given
        val source = AnimeListSourceFake(ongoing = { _, _ -> CallResult.Success(items) })

        //When
        FetchOngoingAnimeListUsecase(source).execute(PAGE)

        //Then
        assertEquals(listOf(PageCall(page = PAGE, sort = SortData.SCORE)), source.ongoingCalls)
    }

    @Test
    fun theListTheSourceAnswersWithIsHandedBackUnchanged() = runTest {
        //Given
        val source = AnimeListSourceFake(ongoing = { _, _ -> CallResult.Success(items) })

        //When
        val result = FetchOngoingAnimeListUsecase(source).execute(PAGE)

        //Then
        assertEquals(CallResult.Success(items), result)
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
            FetchOngoingAnimeListUsecase(
                AnimeListSourceFake(ongoing = { _, _ -> failure })
            ).execute(PAGE)
        }

        //Then
        assertEquals(failures, results)
    }
}
