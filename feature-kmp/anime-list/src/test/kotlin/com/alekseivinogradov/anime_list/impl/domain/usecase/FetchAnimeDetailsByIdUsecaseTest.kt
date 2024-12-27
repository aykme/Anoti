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

class FetchAnimeDetailsByIdUsecaseTest {
    private lateinit var source: AnimeListSource
    private lateinit var usecase: FetchAnimeDetailsByIdUsecase

    @BeforeTest
    fun setup() {
        source = mockk()
        usecase = FetchAnimeDetailsByIdUsecase(source)
    }

    @Test
    fun testFetchAnimeDetailsByIdUsecaseSuccessResult() = runTest {
        //Given
        val randomId = createRandomId()
        val expectedResult: CallResult.Success<ListItemDomain> = createTestSuccessResult(randomId)
        coEvery { source.getItemById(any()) } returns expectedResult

        //When
        val actualResult: CallResult<ListItemDomain> = usecase.execute(randomId)

        //Then
        assertTrue {
            actualResult is CallResult.Success &&
                    actualResult == expectedResult
        }
    }

    @Test
    fun testFetchAnimeDetailsByIdUsecaseHttpErrorResult() = runTest {
        //Given
        val randomId = createRandomId()
        val expectedResult: CallResult.HttpError = createTestHttpErrorResult()
        coEvery { source.getItemById(any()) } returns expectedResult

        //When
        val actualResult: CallResult<ListItemDomain> = usecase.execute(randomId)

        //Then
        assertTrue {
            actualResult is CallResult.HttpError &&
                    actualResult == expectedResult
        }
    }

    @Test
    fun testFetchAnimeDetailsByIdUsecaseOtherErrorResult() = runTest {
        //Given
        val randomId = createRandomId()
        val expectedResult: CallResult.OtherError = createTestOtherErrorResult()
        coEvery { source.getItemById(any()) } returns expectedResult

        //When
        val actualResult: CallResult<ListItemDomain> = usecase.execute(randomId)

        //Then
        assertTrue {
            actualResult is CallResult.OtherError &&
                    actualResult == expectedResult
        }
    }
}
