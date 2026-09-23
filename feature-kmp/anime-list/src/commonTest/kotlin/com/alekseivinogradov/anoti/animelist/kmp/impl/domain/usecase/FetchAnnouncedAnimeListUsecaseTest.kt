package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.animelist.kmp.impl.data.source.fake.AnimeListSourceFake
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.fake.CallResultFake
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class FetchAnnouncedAnimeListUsecaseTest {
    private val maxDelay = 60000 //1 minute
    private val page = 3
    private val sort = SortData.POPULARITY
    private lateinit var source: RecordingAnnouncedSourceFake
    private lateinit var usecase: FetchAnnouncedAnimeListUsecase

    private data class AnnouncedListCall(val page: Int, val sort: SortData)

    // The shared fake answers the same way whatever it is asked, so the arguments it was asked
    // with are recorded here instead.
    private class RecordingAnnouncedSourceFake(
        private val delegate: AnimeListSource
    ) : AnimeListSource by delegate {

        var lastCall: AnnouncedListCall? = null
            private set

        override suspend fun getAnnouncedList(
            page: Int,
            sort: SortData
        ): CallResult<List<ListItemDomain>> {
            lastCall = AnnouncedListCall(page = page, sort = sort)
            return delegate.getAnnouncedList(page = page, sort = sort)
        }
    }

    @Test
    fun testFetchAnnouncedAnimeListUsecaseSuccessResult() = runTest {
        //Given
        initSourceAndUsecase(callResultFake = CallResultFake.SUCCESS)
        val expectedResult: CallResult<List<ListItemDomain>> = source.getAnnouncedList(
            page = page,
            sort = sort
        )

        //When
        val actualResult: CallResult<List<ListItemDomain>> = usecase.execute(page)

        //Then
        assertEquals(AnnouncedListCall(page = page, sort = sort), source.lastCall)
        assertTrue {
            expectedResult is CallResult.Success &&
                actualResult is CallResult.Success &&
                actualResult == expectedResult
        }
    }

    @Test
    fun testFetchAnnouncedAnimeListUsecaseHttpErrorResult() = runTest {
        //Given
        initSourceAndUsecase(callResultFake = CallResultFake.HTTP_ERROR)
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
        initSourceAndUsecase(callResultFake = CallResultFake.OTHER_ERROR)
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

    private fun initSourceAndUsecase(callResultFake: CallResultFake) {
        source = RecordingAnnouncedSourceFake(
            AnimeListSourceFake(
                callResultFake = callResultFake,
                desiredDelay = Random.nextInt(maxDelay).milliseconds
            )
        )
        usecase = FetchAnnouncedAnimeListUsecase(source)
    }
}
