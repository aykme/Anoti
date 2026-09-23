package com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.anoti.animefavorites.kmp.impl.data.source.fake.AnimeFavoritesSourceImplFake
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.fake.CallResultFake
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
        initSourceAndUsecase(callResultFake = CallResultFake.SUCCESS)
        val randomId: Int = createRandomId()
        val expectedResult: CallResult<ListItemDomain> = source.getItemById(randomId)

        //When
        val actualResult: CallResult<ListItemDomain> = usecase.execute(randomId)

        //Then
        assertTrue {
            expectedResult is CallResult.Success &&
                actualResult is CallResult.Success &&
                actualResult == expectedResult
        }
    }

    @Test
    fun testFetchAnimeDetailsByIdUsecaseHttpErrorResult() = runTest {
        //Given
        initSourceAndUsecase(callResultFake = CallResultFake.HTTP_ERROR)
        val randomId: Int = createRandomId()
        val expectedResult: CallResult<ListItemDomain> = source.getItemById(randomId)

        //When
        val actualResult: CallResult<ListItemDomain> = usecase.execute(randomId)

        //Then
        assertTrue {
            expectedResult is CallResult.HttpError &&
                actualResult is CallResult.HttpError &&
                actualResult == expectedResult
        }
    }

    @Test
    fun testFetchAnimeDetailsByIdUsecaseOtherErrorResult() = runTest {
        //Given
        initSourceAndUsecase(callResultFake = CallResultFake.OTHER_ERROR)
        val randomId: Int = createRandomId()
        val expectedResult: CallResult<ListItemDomain> = source.getItemById(randomId)

        //When
        val actualResult: CallResult<ListItemDomain> = usecase.execute(randomId)

        //Then
        assertTrue {
            expectedResult is CallResult.OtherError &&
                actualResult is CallResult.OtherError &&
                actualResult == expectedResult
        }
    }

    private fun initSourceAndUsecase(callResultFake: CallResultFake) {
        source = AnimeFavoritesSourceImplFake(
            callResultFake = callResultFake,
            desiredDelay = Random.nextInt(maxDelay).milliseconds
        )
        usecase = FetchAnimeDetailsByIdUsecase(source)
    }

    private fun createRandomId(): Int = Random.nextInt(Int.MAX_VALUE)
}
