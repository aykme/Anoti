package com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import kotlin.test.Test
import kotlin.test.assertEquals

class SectionStoreLabelToMainStoreIntentMapperTest {

    @Test
    fun theSearchSectionResetLabelRaisesTheResetListPositionFlag() {
        //Given
        val label = SearchSectionStore.Label.ResetListPositionAfterUpdate

        //When
        val intent = mapSearchStoreLabelToMainStoreIntent(label)

        //Then
        assertEquals(
            AnimeListMainStore.Intent.ChangeResetListPositionFlag(isNeedToResetListPosition = true),
            intent
        )
    }
}
