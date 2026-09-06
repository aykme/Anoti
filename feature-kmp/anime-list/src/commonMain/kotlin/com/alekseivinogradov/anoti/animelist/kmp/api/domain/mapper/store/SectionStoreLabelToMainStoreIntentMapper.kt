package com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore

internal fun mapSearchStoreLabelToMainStoreIntent(
    label: SearchSectionStore.Label
): AnimeListMainStore.Intent {
    return when (label) {
        SearchSectionStore.Label.ResetListPositionAfterUpdate -> {
            AnimeListMainStore.Intent.ChangeResetListPositionFlag(
                isNeedToResetListPosition = true
            )
        }
    }
}
