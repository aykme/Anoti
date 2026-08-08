package com.alekseivinogradov.anoti.animelist.kmp.api.domain.model

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

/**
 * A section's (ongoing/announced/search) list content.
 *
 * @param contentType loading state of the list.
 * @param listItems the section's list items.
 * @param enabledExtraEpisodesInfoIds ids currently showing the extra episode-info variant.
 * @param animeDetails extra per-item details fetched for ids in [enabledExtraEpisodesInfoIds].
 */
data class SectionContentDomain(
    val contentType: ContentTypeDomain = ContentTypeDomain.LOADING,
    val listItems: List<ListItemDomain> = emptyList(),
    val enabledExtraEpisodesInfoIds: Set<AnimeId> = setOf(),
    val animeDetails: AnimeDetails = AnimeDetails()
)
