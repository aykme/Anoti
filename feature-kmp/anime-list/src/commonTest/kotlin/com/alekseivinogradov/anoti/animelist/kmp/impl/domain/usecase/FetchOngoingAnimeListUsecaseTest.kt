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

class FetchOngoingAnimeListUsecaseTest {
    private val maxDelay = 60000 //1 minute
    private val page = 3
    private val sort = SortData.SCORE
    private lateinit var source: RecordingOngoingSource
    private lateinit var usecase: FetchOngoingAnimeListUsecase

    private data class OngoingListCall(val page: Int, val sort: SortData)

    // The shared fake answers the same way whatever it is asked, so the arguments it was asked
    // with are recorded here instead.
    private class RecordingOngoingSource(
        private val delegate: AnimeListSource
    ) : AnimeListSource by delegate {

        var lastCall: OngoingListCall? = null
            private set

        override suspend fun getOngoingList(
            page: Int,
            sort: SortData
        ): CallResult<List<ListItemDomain>> {
            lastCall = OngoingListCall(page = page, sort = sort)
            return delegate.getOngoingList(page = page, sort = sort)
        }
    }

    @Test
    fun testFetchOngoingAnimeListUsecaseSuccessResult() = runTest {
        //Given
        initSourceAndUsecase(callResultFake = CallResultFake.SUCCESS)
        val expectedResult: CallResult<List<ListItemDomain>> = source.getOngoingList(
            page = page,
            sort = sort
        )

        //When
        val actualResult: CallResult<List<ListItemDomain>> = usecase.execute(page)

        //Then
        assertEquals(OngoingListCall(page = page, sort = sort), source.lastCall)
        assertTrue {
            expectedResult is CallResult.Success &&
                actualResult is CallResult.Success &&
                actualResult == expectedResult
        }
    }

    @Test
    fun testFetchOngoingAnimeListUsecaseHttpErrorResult() = runTest {
        //Given
        initSourceAndUsecase(callResultFake = CallResultFake.HTTP_ERROR)
        val expectedResult: CallResult<List<ListItemDomain>> = source.getOngoingList(
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
    fun testFetchOngoingAnimeListUsecaseOtherErrorResult() = runTest {
        //Given
        initSourceAndUsecase(callResultFake = CallResultFake.OTHER_ERROR)
        val expectedResult: CallResult<List<ListItemDomain>> = source.getOngoingList(
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

    private fun initSourceAndUsecase(callResultFake: CallResultFake) {
        source = RecordingOngoingSource(
            AnimeListSourceImplFake(
                callResultFake = callResultFake,
                desiredDelay = Random.nextInt(maxDelay).milliseconds
            )
        )
        usecase = FetchOngoingAnimeListUsecase(source)
    }
}
