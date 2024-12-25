package com.alekseivinogradov.anime_favorites.impl.domain.usecase

import com.alekseivinogradov.anime_favorites.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.anime_favorites.impl.data.source.AnimeFavoritesSourceImplFake
import com.alekseivinogradov.anime_favorites.impl.data.source.DesiredResult
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class FetchAnimeDetailsByIdUsecaseTest {
    private val maxDelay = 60000 //1 minute
    private lateinit var source: AnimeFavoritesSource
    private lateinit var usecase: FetchAnimeDetailsByIdUsecase

    @Test
    fun testFetchAnimeDetailsByIdUsecaseSuccessResult() = runTest {
        //Given
        initSourceAndUsecase(desiredResult = DesiredResult.SUCCESS)
        val randomId = getRandomId()
        val expectedResult = source.getItemById(randomId)

        //When
        val result = usecase.execute(randomId)

        //Then
        assertTrue {
            expectedResult is CallResult.Success &&
                    result is CallResult.Success &&
                    result.value == expectedResult.value
        }
    }

    @Test
    fun testFetchAnimeDetailsByIdUsecaseHttpErrorResult() = runTest {
        //Given
        initSourceAndUsecase(desiredResult = DesiredResult.HTTP_ERROR)
        val randomId = getRandomId()
        val expectedResult = source.getItemById(randomId)

        //When
        val result = usecase.execute(randomId)

        //Then
        assertTrue {
            expectedResult is CallResult.HttpError &&
                    result is CallResult.HttpError &&
                    result.code == expectedResult.code &&
                    result.throwable == expectedResult.throwable
        }
    }

    @Test
    fun testFetchAnimeDetailsByIdUsecaseOtherErrorResult() = runTest {
        //Given
        initSourceAndUsecase(desiredResult = DesiredResult.OTHER_ERROR)
        val randomId = getRandomId()
        val expectedResult = source.getItemById(randomId)

        //When
        val result = usecase.execute(randomId)

        //Then
        assertTrue {
            expectedResult is CallResult.OtherError &&
                    result is CallResult.OtherError &&
                    result.throwable == expectedResult.throwable
        }
    }

    private fun initSourceAndUsecase(desiredResult: DesiredResult) {
        source = AnimeFavoritesSourceImplFake(
            desiredResult = desiredResult,
            desiredDelay = Random.nextInt(maxDelay).milliseconds
        )
        usecase = FetchAnimeDetailsByIdUsecase(source)
    }

    private fun getRandomId() = Random.nextInt(Int.MAX_VALUE)
}
