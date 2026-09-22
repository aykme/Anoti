package com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.mapper

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseStoreLabelToMainStoreIntentMapperTest {

    @Test
    fun aFinishedNewEpisodeStatusResetTriggersTheBackgroundUpdateOfEveryItem() {
        //Given
        val label = AnimeDatabaseStore.Label.ResetAllItemsNewEpisodeStatusWasFinished

        //When
        val intent = mapDatabaseStoreLabelToMainStoreIntent(label)

        //Then
        assertEquals(AnimeFavoritesMainStore.Intent.UpdateAllItemsInBackground, intent)
    }
}
