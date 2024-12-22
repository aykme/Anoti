package com.alekseivinogradov.anime_favorites.impl.domain.usecase

import com.alekseivinogradov.anime_favorites.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.anime_favorites.impl.data.source.AnimeFavoritesSourceImplFake
import com.alekseivinogradov.anime_favorites.impl.data.source.DesiredResult
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class FetchAnimeDetailsByIdUsecaseTest {
    private val maxDelay = 600000 //10 minutes
    private var randomDelay: Duration? = null
    private var randomId: Int? = null
    private var source: AnimeFavoritesSource? = null
    private var usecase: FetchAnimeDetailsByIdUsecase? = null

    @BeforeTest
    fun setup() {
        randomDelay = Random.nextInt(maxDelay).milliseconds
        randomId = Random.nextInt(Int.MAX_VALUE)
    }

    @Test
    fun testFetchAnimeDetailsByIdUsecaseSuccessResult() = runTest {
        //Given
        initSourceAndUsecase(desiredResult = DesiredResult.SUCCESS)
        val expectedResult = source!!.getItemById(randomId!!)

        //When
        val result = usecase!!.execute(randomId!!)

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
        val expectedResult = source!!.getItemById(randomId!!)

        //When
        val result = usecase!!.execute(randomId!!)

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
        val expectedResult = source!!.getItemById(randomId!!)

        //When
        val result = usecase!!.execute(randomId!!)

        //Then
        assertTrue {
            expectedResult is CallResult.OtherError &&
                    result is CallResult.OtherError &&
                    result.throwable == expectedResult.throwable
        }
    }

    @AfterTest
    fun afterTest() {
        randomDelay = null
        randomId = null
        source = null
        usecase = null
    }

    private fun initSourceAndUsecase(desiredResult: DesiredResult) {
        source = AnimeFavoritesSourceImplFake(
            desiredResult = desiredResult,
            desiredDelay = randomDelay!!
        )
        usecase = FetchAnimeDetailsByIdUsecase(source!!)
    }
}
