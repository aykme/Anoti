package com.alekseivinogradov.anoti.webpageopener.android.impl.domain.usecase

import android.app.Application
import android.content.Intent
import android.net.Uri
import com.alekseivinogradov.anoti.webpageopener.android.impl.di.DiWebPageOpenerPlatformComponent
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class OpenWebPageUsecaseImplTest {

    private val application: Application = RuntimeEnvironment.getApplication()

    private val usecase = object : DiWebPageOpenerPlatformComponent {}
        .provideOpenWebPageUsecase(appContext = application)

    @Test
    fun aPageIsStartedAsABrowsableViewInATaskOfItsOwn() = runTest {
        //Given
        val url = PAGE_URL

        //When
        val opened = usecase.execute(url)

        //Then
        val started = shadowOf(application).nextStartedActivity
        assertTrue(opened)
        assertEquals(Intent.ACTION_VIEW, started.action)
        assertEquals(Uri.parse(url), started.data)
        assertTrue(started.hasCategory(Intent.CATEGORY_BROWSABLE))
        assertTrue(started.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun withNoAppForThePageItAnswersFalseWithoutThrowing() = runTest {
        //Given
        // Robolectric then throws ActivityNotFoundException for an intent nothing resolves.
        shadowOf(application).checkActivities(true)

        //When
        val opened = usecase.execute(PAGE_URL)

        //Then
        assertFalse(opened)
    }
}

private const val PAGE_URL = "https://example.com/page/"
