package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.impl.data.source.fake.AnimeListSourceFake
import com.alekseivinogradov.anoti.animelist.kmp.impl.data.source.fake.SearchCall
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val PAGE = 3
private const val HTTP_ERROR_CODE = 500
private const val SEARCH_TEXT = "frieren"

class FetchAnimeListBySearchUsecaseTest {

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
    fun thePageAndSearchTextAskedForAreFetchedSortedByScore() = runTest {
        //Given
        val source = AnimeListSourceFake(search = { _, _, _ -> CallResult.Success(items) })

        //When
        FetchAnimeListBySearchUsecase(source).execute(page = PAGE, searchText = SEARCH_TEXT)

        //Then
        assertEquals(
            listOf(SearchCall(page = PAGE, search = SEARCH_TEXT, sort = SortData.SCORE)),
            source.searchCalls
        )
    }

    @Test
    fun theListTheSourceAnswersWithIsHandedBackUnchanged() = runTest {
        //Given
        val source = AnimeListSourceFake(search = { _, _, _ -> CallResult.Success(items) })

        //When
        val result = FetchAnimeListBySearchUsecase(source)
            .execute(page = PAGE, searchText = SEARCH_TEXT)

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
            FetchAnimeListBySearchUsecase(
                AnimeListSourceFake(search = { _, _, _ -> failure })
            ).execute(page = PAGE, searchText = SEARCH_TEXT)
        }

        //Then
        assertEquals(failures, results)
    }
}
