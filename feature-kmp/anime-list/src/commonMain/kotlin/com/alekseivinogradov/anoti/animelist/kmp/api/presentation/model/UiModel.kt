package com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model

/**
 * What the anime list screen should render.
 *
 * @param selectedSection currently selected section.
 * @param search state of the search bar.
 * @param contentType loading state of the selected section's list.
 * @param listContent the selected section's list content.
 */
data class UiModel(
    val selectedSection: SectionHatUi = SectionHatUi.ONGOINGS,
    val search: SearchUi = SearchUi.HIDDEN,
    val contentType: ContentTypeUi = ContentTypeUi.LOADING,
    val listContent: ListContentUi = ListContentUi()
)
