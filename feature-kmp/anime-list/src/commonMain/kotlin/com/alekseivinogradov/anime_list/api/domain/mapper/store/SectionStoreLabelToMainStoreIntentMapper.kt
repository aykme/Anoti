package com.alekseivinogradov.anime_list.api.domain.mapper.store

import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore

fun mapOngoingStoreLabelToMainStoreIntent(
    label: OngoingSectionStore.Label
): AnimeListMainStore.Intent {
    return when (label) {
        OngoingSectionStore.Label.ResetListPositionAfterUpdate -> {
            AnimeListMainStore.Intent.ChangeResetListPositionAfterUpdateStatus(
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
            AnimeListMainStore.Intent.ChangeResetListPositionAfterUpdateStatus(
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
            AnimeListMainStore.Intent.ChangeResetListPositionAfterUpdateStatus(
                isNeedToResetListPosition = true
            )
        }
    }
}