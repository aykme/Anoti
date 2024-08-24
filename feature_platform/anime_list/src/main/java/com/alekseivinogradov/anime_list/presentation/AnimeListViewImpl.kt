package com.alekseivinogradov.anime_list.presentation

import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.isVisible
import com.alekseivinogradov.anime_list.api.model.UiContentType
import com.alekseivinogradov.anime_list.api.model.UiSearch
import com.alekseivinogradov.anime_list.api.model.UiSection
import com.alekseivinogradov.anime_list.api.model.list_content.UiListItem
import com.alekseivinogradov.anime_list.api.presentation.AnimeListView
import com.alekseivinogradov.anime_list.databinding.FragmentAnimeListBinding
import com.alekseivinogradov.anime_list.presentation.adapter.AnimeListAdapter
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import com.alekseivinogradov.theme.R as theme_R

internal class AnimeListViewImpl(
    private val binding: FragmentAnimeListBinding
) : AnimeListView, BaseMviView<AnimeListView.UiModel, AnimeListView.UiEvent>() {

    private val context
        get() = binding.root.context

    private val activeMenuColor
        get() = context.getColor(theme_R.color.pink)

    private val defaultMenuColor
        get() = context.getColor(theme_R.color.white_transparent)

    private val episodesInfoClickCallback = { itemIndex: Int ->
        dispatch(AnimeListView.UiEvent.EpisodesInfoClick(itemIndex))
    }

    private val notificationClickCallback = { itemIndex: Int ->
        dispatch(AnimeListView.UiEvent.NotificationClick(itemIndex))
    }

    private val adapter = AnimeListAdapter(
        episodesInfoClickCallback = episodesInfoClickCallback,
        notificationClickCallback = notificationClickCallback
    )

    init {
        initClickListeners()
        initSearchTextChangedListener()
    }

    override val renderer: ViewRenderer<AnimeListView.UiModel> = diff {
        diff(
            get = ::getSelectedSection,
            set = ::setSelectedSection
        )

        diff(
            get = ::getSearch,
            set = ::setSearch
        )

        diff(
            get = ::getContentType,
            set = ::setContentType
        )

        diff(
            get = ::getOngoingListItems,
            set = ::setOngoingListItems
        )

        diff(
            get = ::getAnnouncedListItems,
            set = ::setAnnouncedListItems
        )

        diff(
            get = ::getSearchListItems,
            set = ::setSearchListItems
        )
    }

    private fun initClickListeners() {
        with(binding) {
            ongoingButton.setOnClickListener {
                dispatch(AnimeListView.UiEvent.OngoingsSectionClick)
            }
            announcedButton.setOnClickListener {
                dispatch(AnimeListView.UiEvent.AnnouncedSectionClick)
            }
            searchButton.setOnClickListener {
                dispatch(AnimeListView.UiEvent.SearchSectionClick)
            }
            searchCancelButton.setOnClickListener {
                dispatch(AnimeListView.UiEvent.CancelSearchClick)
            }
        }
    }

    private fun initSearchTextChangedListener() {
        binding.searchEditText.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) = Unit

                override fun afterTextChanged(s: Editable?) {
                    dispatch(AnimeListView.UiEvent.SearchTextChange(s?.toString().orEmpty()))
                }
            }
        )
    }

    private fun getSelectedSection(uiModel: AnimeListView.UiModel): UiSection {
        return uiModel.selectedSection
    }

    private fun setSelectedSection(selectedSection: UiSection) {
        with(binding) {
            when (selectedSection) {
                UiSection.ONGOINGS -> {
                    ongoingButton.setTextColor(activeMenuColor)
                    announcedButton.setTextColor(defaultMenuColor)
                    searchButton.setColorFilter(defaultMenuColor)
                }

                UiSection.ANNOUNCED -> {
                    announcedButton.setTextColor(activeMenuColor)
                    ongoingButton.setTextColor(defaultMenuColor)
                    searchButton.setColorFilter(defaultMenuColor)
                }

                UiSection.SEARCH -> {
                    searchButton.setColorFilter(activeMenuColor)
                    announcedButton.setTextColor(defaultMenuColor)
                    ongoingButton.setTextColor(defaultMenuColor)
                }
            }
        }
        setSwipeRefreshListener(selectedSection)
    }

    private fun setSwipeRefreshListener(selectedSection: UiSection) {
        with(binding.swipeRefreshLayout) {
            when (selectedSection) {
                UiSection.ONGOINGS -> setOnRefreshListener {
                    dispatch(AnimeListView.UiEvent.UpdateOngoingsSection)
                }

                UiSection.ANNOUNCED -> setOnRefreshListener {
                    dispatch(AnimeListView.UiEvent.UpdateAnnouncedSection)
                }

                UiSection.SEARCH -> setOnRefreshListener {
                    dispatch(AnimeListView.UiEvent.UpdateSearchSection)
                }
            }
        }
    }

    private fun getSearch(uiModel: AnimeListView.UiModel): UiSearch {
        return uiModel.search
    }

    private fun setSearch(search: UiSearch) {
        with(binding) {
            when (search) {
                UiSearch.HIDEN -> {
                    searchInputLayout.isVisible = false
                    searchCancelButton.isVisible = false
                    ongoingButton.isVisible = true
                    verticalDivider.isVisible = true
                    announcedButton.isVisible = true
                    searchButton.isVisible = true
                    searchButtonShadow.isVisible = true
                }

                UiSearch.SHOWN -> {
                    ongoingButton.isVisible = false
                    verticalDivider.isVisible = false
                    announcedButton.isVisible = false
                    searchButton.isVisible = false
                    searchButtonShadow.isVisible = false
                    searchInputLayout.isVisible = true
                    searchCancelButton.isVisible = true
                }
            }
        }
    }

    private fun getContentType(uiModel: AnimeListView.UiModel): UiContentType {
        return uiModel.contentType
    }

    private fun setContentType(contentType: UiContentType) {
        with(binding) {
            when (contentType) {
                UiContentType.LOADED -> {
                    connectionStatusImage.isVisible = false
                    animeListRv.isVisible = true
                }

                UiContentType.LOADING -> {
                    animeListRv.isVisible = false
                    connectionStatusImage.isVisible = true
                }

                UiContentType.NO_DATA -> {
                    animeListRv.isVisible = false
                    connectionStatusImage.isVisible = true
                }
            }
        }
    }

    private fun getOngoingListItems(uiModel: AnimeListView.UiModel): ListItemsWithSelectedSection {
        return ListItemsWithSelectedSection(
            listItems = uiModel.ongoingListItems,
            selectedSection = uiModel.selectedSection
        )
    }

    private fun setOngoingListItems(listItemsWithSelectedSection: ListItemsWithSelectedSection) {
        if (listItemsWithSelectedSection.selectedSection == UiSection.ONGOINGS) {

        }
    }

    private fun getAnnouncedListItems(
        uiModel: AnimeListView.UiModel
    ): ListItemsWithSelectedSection {
        return ListItemsWithSelectedSection(
            listItems = uiModel.announcedListItems,
            selectedSection = uiModel.selectedSection
        )
    }

    private fun setAnnouncedListItems(listItemsWithSelectedSection: ListItemsWithSelectedSection) {
        if (listItemsWithSelectedSection.selectedSection == UiSection.ANNOUNCED) {

        }
    }

    private fun getSearchListItems(uiModel: AnimeListView.UiModel): ListItemsWithSelectedSection {
        return ListItemsWithSelectedSection(
            listItems = uiModel.searchListItems,
            selectedSection = uiModel.selectedSection
        )
    }

    private fun setSearchListItems(listItemsWithSelectedSection: ListItemsWithSelectedSection) {
        if (listItemsWithSelectedSection.selectedSection == UiSection.SEARCH) {

        }
    }

    data class ListItemsWithSelectedSection(
        val listItems: List<UiListItem>,
        val selectedSection: UiSection
    )
}
