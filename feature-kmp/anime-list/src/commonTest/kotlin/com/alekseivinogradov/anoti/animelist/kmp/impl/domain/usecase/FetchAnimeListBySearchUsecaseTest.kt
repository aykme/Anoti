package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.animelist.kmp.impl.data.source.fake.AnimeListSourceImplFake
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.fake.CallResultFake
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class FetchAnimeListBySearchUsecaseTest {
    private val maxDelay = 60000 //1 minute
    private val page = 3
    private val searchText = "search"
    private val sort = SortData.SCORE
    private lateinit var source: RecordingSearchSource
    private lateinit var usecase: FetchAnimeListBySearchUsecase

    private data class SearchListCall(val page: Int, val search: String, val sort: SortData)

    // The shared fake answers the same way whatever it is asked, so the arguments it was asked
    // with are recorded here instead.
    private class RecordingSearchSource(
        private val delegate: AnimeListSource
    ) : AnimeListSource by delegate {

        var lastCall: SearchListCall? = null
            private set

        override suspend fun getListBySearch(
            page: Int,
            search: String,
            sort: SortData
        ): CallResult<List<ListItemDomain>> {
            lastCall = SearchListCall(page = page, search = search, sort = sort)
            return delegate.getListBySearch(page = page, search = search, sort = sort)
        }
    }

    @Test
    fun testFetchAnimeListBySearchUsecaseSuccessResult() = runTest {
        //Given
        initSourceAndUsecase(callResultFake = CallResultFake.SUCCESS)
        val expectedResult: CallResult<List<ListItemDomain>> = source.getListBySearch(
            page = page,
            search = searchText,
            sort = sort
        )

        //When
        val actualResult: CallResult<List<ListItemDomain>> = usecase.execute(
            page = page,
            searchText = searchText
        )

        //Then
        assertEquals(
            SearchListCall(page = page, search = searchText, sort = sort),
            source.lastCall
        )
        assertTrue {
            expectedResult is CallResult.Success &&
                actualResult is CallResult.Success &&
                actualResult == expectedResult
        }
    }

    @Test
    fun testFetchAnimeListBySearchUsecaseHttpErrorResult() = runTest {
        //Given
        initSourceAndUsecase(callResultFake = CallResultFake.HTTP_ERROR)
        val expectedResult: CallResult<List<ListItemDomain>> = source.getListBySearch(
            page = page,
            search = searchText,
            sort = sort
        )

        //When
        val actualResult: CallResult<List<ListItemDomain>> = usecase.execute(
            page = page,
            searchText = searchText
        )

        //Then
        assertTrue {
            expectedResult is CallResult.HttpError &&
                actualResult is CallResult.HttpError &&
                actualResult == expectedResult
        }
    }

    @Test
    fun testFetchAnimeListBySearchUsecaseOtherErrorResult() = runTest {
        //Given
        initSourceAndUsecase(callResultFake = CallResultFake.OTHER_ERROR)
        val expectedResult: CallResult<List<ListItemDomain>> = source.getListBySearch(
            page = page,
            search = searchText,
            sort = sort
        )

        //When
        val actualResult: CallResult<List<ListItemDomain>> = usecase.execute(
            page = page,
            searchText = searchText
        )

        //Then
        assertTrue {
            expectedResult is CallResult.OtherError &&
                actualResult is CallResult.OtherError &&
                actualResult == expectedResult
        }
    }

    private fun initSourceAndUsecase(callResultFake: CallResultFake) {
        source = RecordingSearchSource(
            AnimeListSourceImplFake(
                callResultFake = callResultFake,
                desiredDelay = Random.nextInt(maxDelay).milliseconds
            )
        )
        usecase = FetchAnimeListBySearchUsecase(source)
    }
}
