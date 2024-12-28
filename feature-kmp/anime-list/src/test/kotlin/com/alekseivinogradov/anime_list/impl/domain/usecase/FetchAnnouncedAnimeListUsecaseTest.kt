package com.alekseivinogradov.anime_list.impl.domain.usecase

import com.alekseivinogradov.anime_base.api.data.model.SortData
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

class FetchAnnouncedAnimeListUsecaseTest {
    private val maxDelay = 60000 //1 minute
    private val page = 1
    private val sort = SortData.POPULARITY.value
    private lateinit var source: AnimeListSource
    private lateinit var usecase: FetchAnnouncedAnimeListUsecase

    @Test
    fun testFetchAnnouncedAnimeListUsecaseSuccessResult() = runTest {
        //Given
        initSourceAndUsecase(desiredCallResult = DesiredCallResult.SUCCESS)
        val expectedResult: CallResult<List<ListItemDomain>> = source.getAnnouncedList(
            page = page,
            sort = sort
        )

        //When
        val actualResult: CallResult<List<ListItemDomain>> = usecase.execute(page)

        //Then
        assertTrue {
            expectedResult is CallResult.Success &&
                    actualResult is CallResult.Success &&
                    actualResult == expectedResult
        }
    }

    @Test
    fun testFetchAnnouncedAnimeListUsecaseHttpErrorResult() = runTest {
        //Given
        initSourceAndUsecase(desiredCallResult = DesiredCallResult.HTTP_ERROR)
        val expectedResult: CallResult<List<ListItemDomain>> = source.getAnnouncedList(
            page = page,
            sort = sort
        )

        //When
        val actualResult: CallResult<List<ListItemDomain>> = usecase.execute(page)

        //Then
        assertTrue {
            expectedResult is CallResult.HttpError &&
                    actualResult is CallResult.HttpError &&
                    actualResult == expectedResult
        }
    }

    @Test
    fun testFetchAnnouncedAnimeListUsecaseOtherErrorResult() = runTest {
        //Given
        initSourceAndUsecase(desiredCallResult = DesiredCallResult.OTHER_ERROR)
        val expectedResult: CallResult<List<ListItemDomain>> = source.getAnnouncedList(
            page = page,
            sort = sort
        )

        //When
        val actualResult: CallResult<List<ListItemDomain>> = usecase.execute(page)

        //Then
        assertTrue {
            expectedResult is CallResult.OtherError &&
                    actualResult is CallResult.OtherError &&
                    actualResult == expectedResult
        }
    }

    private fun initSourceAndUsecase(desiredCallResult: DesiredCallResult) {
        source = AnimeListSourceImplFake(
            desiredCallResult = desiredCallResult,
            desiredDelay = Random.nextInt(maxDelay).milliseconds
        )
        usecase = FetchAnnouncedAnimeListUsecase(source)
    }
}
