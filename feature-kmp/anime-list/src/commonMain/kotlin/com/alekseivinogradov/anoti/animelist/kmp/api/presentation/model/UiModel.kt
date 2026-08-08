package com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model

data class UiModel(
    val selectedSection: SectionHatUi = SectionHatUi.ONGOINGS,
    val search: SearchUi = SearchUi.HIDDEN,
    val contentType: ContentTypeUi = ContentTypeUi.LOADING,
    val listContent: ListContentUi = ListContentUi()
)
