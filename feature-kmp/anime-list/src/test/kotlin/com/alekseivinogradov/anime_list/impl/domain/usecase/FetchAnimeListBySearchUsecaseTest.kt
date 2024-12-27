package com.alekseivinogradov.anime_list.impl.domain.usecase

import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.source.AnimeListSource
import com.alekseivinogradov.network.api.domain.model.CallResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class FetchAnimeListBySearchUsecaseTest {
    private lateinit var source: AnimeListSource
    private lateinit var usecase: FetchAnimeListBySearchUsecase

    @BeforeTest
    fun setup() {
        source = mockk()
        usecase = FetchAnimeListBySearchUsecase(source)
    }

    @Test
    fun testFetchAnimeListBySearchUsecaseSuccessResult() = runTest {
        //Given
        val expectedResult: CallResult.Success<List<ListItemDomain>> =
            createTestListSuccessResult(itemNumber = 10)
        coEvery { source.getListBySearch(any(), any(), any()) } returns expectedResult

        //When
        val actualResult: CallResult<List<ListItemDomain>> = usecase.execute(
            page = 1,
            searchText = ""
        )

        //Then
        assertTrue {
            actualResult is CallResult.Success &&
                    actualResult == expectedResult
        }
    }

    @Test
    fun testFetchAnimeListBySearchUsecaseHttpErrorResult() = runTest {
        //Given
        val expectedResult: CallResult.HttpError = createTestHttpErrorResult()
        coEvery { source.getListBySearch(any(), any(), any()) } returns expectedResult

        //When
        val actualResult: CallResult<List<ListItemDomain>> = usecase.execute(
            page = 1,
            searchText = ""
        )

        //Then
        assertTrue {
            actualResult is CallResult.HttpError &&
                    actualResult == expectedResult
        }
    }

    @Test
    fun testFetchAnimeListBySearchUsecaseOtherErrorResult() = runTest {
        //Given
        val expectedResult: CallResult.OtherError = createTestOtherErrorResult()
        coEvery { source.getListBySearch(any(), any(), any()) } returns expectedResult

        //When
        val actualResult: CallResult<List<ListItemDomain>> = usecase.execute(
            page = 1,
            searchText = ""
        )

        //Then
        assertTrue {
            actualResult is CallResult.OtherError &&
                    actualResult == expectedResult
        }
    }
}
