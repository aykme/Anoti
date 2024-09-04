package com.alekseivinogradov.anime_list.api.presentation.mapper.model

import com.alekseivinogradov.anime_list.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anime_list.api.presentation.model.SearchUi
import com.alekseivinogradov.anime_list.api.presentation.model.SectionUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.ListItemUi

data class UiModel(
    val selectedSection: SectionUi = SectionUi.ONGOINGS,
    val search: SearchUi = SearchUi.HIDEN,
    val contentType: ContentTypeUi = ContentTypeUi.LOADING,
    val ongoingListItems: List<ListItemUi> = listOf(),
    val announcedListItems: List<ListItemUi> = listOf(),
    val searchListItems: List<ListItemUi> = listOf()
)