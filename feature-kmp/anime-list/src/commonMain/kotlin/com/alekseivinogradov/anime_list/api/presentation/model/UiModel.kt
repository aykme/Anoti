package com.alekseivinogradov.anime_list.api.presentation.model

data class UiModel(
    val selectedSection: SectionHatUi = SectionHatUi.ONGOINGS,
    val search: SearchUi = SearchUi.HIDEN,
    val contentType: ContentTypeUi = ContentTypeUi.LOADING,
    val listContent: ListContentUi = ListContentUi()
)
