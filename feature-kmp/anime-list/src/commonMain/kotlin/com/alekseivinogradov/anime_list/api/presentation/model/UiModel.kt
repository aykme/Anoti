package com.alekseivinogradov.anime_list.api.presentation.model

import app.cash.paging.PagingData
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.ListItemUi

data class UiModel(
    val selectedSection: SectionHatUi = SectionHatUi.ONGOINGS,
    val search: SearchUi = SearchUi.HIDEN,
    val contentType: ContentTypeUi = ContentTypeUi.LOADING,
    val listItems: PagingData<ListItemUi> = PagingData.empty(),
)
