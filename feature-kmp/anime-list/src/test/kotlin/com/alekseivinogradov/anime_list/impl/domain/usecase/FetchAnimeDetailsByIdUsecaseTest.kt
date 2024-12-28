package com.alekseivinogradov.anime_list.impl.domain.usecase

import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.source.AnimeListSource
import com.alekseivinogradov.anime_list.impl.data.source.fake.AnimeListSourceImplFake
import com.alekseivinogradov.network.api.domain.model.CallResult
import com.alekseivinogradov.network.api.domain.model.test.DesiredCallResult
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class FetchAnimeDetailsByIdUsecaseTest {
    private val maxDelay = 60000 //1 minute
    private lateinit var source: AnimeListSource
    private lateinit var usecase: FetchAnimeDetailsByIdUsecase

    @Test
    fun testFetchAnimeDetailsByIdUsecaseSuccessResult() = runTest {
        //Given
        initSourceAndUsecase(desiredCallResult = DesiredCallResult.SUCCESS)
        val randomId: Int = createRandomId()
        val expectedResult: CallResult<ListItemDomain> = source.getItemById(randomId)

        //When
        val actualResult: CallResult<ListItemDomain> = usecase.execute(randomId)

        //Then
        assertTrue {
            expectedResult is CallResult.Success &&
                    actualResult is CallResult.Success &&
                    actualResult.value == expectedResult.value
        }
    }

    @Test
    fun testFetchAnimeDetailsByIdUsecaseHttpErrorResult() = runTest {
        //Given
        initSourceAndUsecase(desiredCallResult = DesiredCallResult.HTTP_ERROR)
        val randomId: Int = createRandomId()
        val expectedResult: CallResult<ListItemDomain> = source.getItemById(randomId)

        //When
        val actualResult: CallResult<ListItemDomain> = usecase.execute(randomId)

        //Then
        assertTrue {
            expectedResult is CallResult.HttpError &&
                    actualResult is CallResult.HttpError &&
                    actualResult.code == expectedResult.code &&
                    actualResult.throwable == expectedResult.throwable
        }
    }

    @Test
    fun testFetchAnimeDetailsByIdUsecaseOtherErrorResult() = runTest {
        //Given
        initSourceAndUsecase(desiredCallResult = DesiredCallResult.OTHER_ERROR)
        val randomId: Int = createRandomId()
        val expectedResult: CallResult<ListItemDomain> = source.getItemById(randomId)

        //When
        val actualResult: CallResult<ListItemDomain> = usecase.execute(randomId)

        //Then
        assertTrue {
            expectedResult is CallResult.OtherError &&
                    actualResult is CallResult.OtherError &&
                    actualResult.throwable == expectedResult.throwable
        }
    }

    private fun initSourceAndUsecase(desiredCallResult: DesiredCallResult) {
        source = AnimeListSourceImplFake(
            desiredCallResult = desiredCallResult,
            desiredDelay = Random.nextInt(maxDelay).milliseconds
        )
        usecase = FetchAnimeDetailsByIdUsecase(source)
    }

    private fun createRandomId(): Int = Random.nextInt(Int.MAX_VALUE)
}
