package com.alekseivinogradov.anime_list.api.domain.mapper.store

import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore

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
