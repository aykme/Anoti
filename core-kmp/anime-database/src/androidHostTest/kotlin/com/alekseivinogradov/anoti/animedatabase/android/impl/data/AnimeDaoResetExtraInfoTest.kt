package com.alekseivinogradov.anoti.animedatabase.android.impl.data

import android.content.Context
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.ReleaseStatusDb
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.ANIME_TABLE_NAME
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.model.AnimeDbEntity
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

private const val EXPANDED_ID = 21
private const val COLLAPSED_ID = 22
private const val NEXT_EPISODE_AT = "2026-10-18T17:15:00+03:00"

/** The real query behind the extra-info reset, which the shared fakes only imitate. */
@RunWith(RobolectricTestRunner::class)
class AnimeDaoResetExtraInfoTest {

    @Test
    fun theResetTurnsTheModeOffAndKeepsTheNextEpisodeDate() = runTest {
        //Given
        val context: Context = RuntimeEnvironment.getApplication()
        context.deleteDatabase(ANIME_TABLE_NAME)
        val database = getAnimeDatabase(context)
        val dao = database.animeDao()
        dao.insert(entity(EXPANDED_ID, isExtraInfoEnabled = true))
        dao.insert(entity(COLLAPSED_ID, isExtraInfoEnabled = false))

        //When
        dao.resetAllItemsExtraInfo()
        val items = dao.getAllItems().associateBy(AnimeDbEntity::id)
        database.close()

        //Then
        assertEquals(entity(EXPANDED_ID, isExtraInfoEnabled = false), items.getValue(EXPANDED_ID))
        assertEquals(entity(COLLAPSED_ID, isExtraInfoEnabled = false), items.getValue(COLLAPSED_ID))
    }

    private fun entity(id: AnimeId, isExtraInfoEnabled: Boolean) = AnimeDbEntity(
        id = id,
        name = "Anime $id",
        imageUrl = null,
        episodesAired = 3,
        episodesTotal = 12,
        nextEpisodeAt = NEXT_EPISODE_AT,
        airedOn = "2026-10-04",
        releasedOn = null,
        score = 8.0F,
        releaseStatus = ReleaseStatusDb.ONGOING,
        episodesViewed = 1,
        isNewEpisode = false,
        isExtraInfoEnabled = isExtraInfoEnabled
    )
}
