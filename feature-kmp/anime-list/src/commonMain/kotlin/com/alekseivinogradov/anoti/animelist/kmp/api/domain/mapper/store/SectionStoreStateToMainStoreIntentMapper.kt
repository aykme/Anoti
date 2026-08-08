package com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore

internal fun mapOngoingStoreStateToMainStoreIntent(
    state: OngoingSectionStore.State
): AnimeListMainStore.Intent {
    return AnimeListMainStore.Intent.UpdateOngoingContent(
        content = state.sectionContent
    )
}

internal fun mapAnnouncedStoreStateToMainStoreIntent(
    state: AnnouncedSectionStore.State
): AnimeListMainStore.Intent {
    return AnimeListMainStore.Intent.UpdateAnnouncedContent(
        content = state.sectionContent
    )
}

internal fun mapSearchStoreStateToMainStoreIntent(
    state: SearchSectionStore.State
): AnimeListMainStore.Intent {
    return AnimeListMainStore.Intent.UpdateSearchContent(
        content = state.sectionContent
    )
}
