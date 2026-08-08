package com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore

fun mapOngoingStoreLabelToMainStoreIntent(
    label: OngoingSectionStore.Label
): AnimeListMainStore.Intent {
    return when (label) {
        OngoingSectionStore.Label.ResetListPositionAfterUpdate -> {
            AnimeListMainStore.Intent.ChangeResetListPositionFlag(
                isNeedToResetListPosition = true
            )
        }
    }
}

internal fun mapAnnouncedStoreLabelToMainStoreIntent(
    label: AnnouncedSectionStore.Label
): AnimeListMainStore.Intent {
    return when (label) {
        AnnouncedSectionStore.Label.ResetListPositionAfterUpdate -> {
            AnimeListMainStore.Intent.ChangeResetListPositionFlag(
                isNeedToResetListPosition = true
            )
        }
    }
}

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
