package com.alekseivinogradov.anime_list.presentation

import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.alekseivinogradov.anime_list.R
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
    private val viewBinding: FragmentAnimeListBinding
) : AnimeListView, BaseMviView<AnimeListView.UiModel, AnimeListView.UiEvent>() {

    private val context
        get() = viewBinding.root.context

    private val activeColor
        get() = context.getColor(theme_R.color.pink)

    private val defaultColor
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
        setCommonFields()
        initClickListeners()
        initSearchTextChangedListener()
        initRv()
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
        )/**/
    }

    private fun setCommonFields() {
        with(viewBinding) {
            ongoingButton.text = context.applicationContext.getString(R.string.ongoings)
            announcedButton.text = context.applicationContext.getString(R.string.announced)
            searchInputLayout.hint = context.applicationContext.getString(R.string.search_hint)
        }
    }

    private fun initClickListeners() {
        with(viewBinding) {
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
        viewBinding.searchEditText.addTextChangedListener(
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

    private fun initRv() {
        with(viewBinding) {
            animeListRv.adapter = adapter
            animeListRv.layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )
            animeListRv.itemAnimator = null
        }
    }

    private fun getSelectedSection(uiModel: AnimeListView.UiModel): UiSection {
        return uiModel.selectedSection
    }

    private fun setSelectedSection(selectedSection: UiSection) {
        with(viewBinding) {
            when (selectedSection) {
                UiSection.ONGOINGS -> {
                    ongoingButton.setTextColor(activeColor)
                    announcedButton.setTextColor(defaultColor)
                    searchButton.setColorFilter(defaultColor)
                }

                UiSection.ANNOUNCED -> {
                    announcedButton.setTextColor(activeColor)
                    ongoingButton.setTextColor(defaultColor)
                    searchButton.setColorFilter(defaultColor)
                }

                UiSection.SEARCH -> {
                    searchButton.setColorFilter(activeColor)
                    announcedButton.setTextColor(defaultColor)
                    ongoingButton.setTextColor(defaultColor)
                }
            }
        }
        setSwipeRefreshListener(selectedSection)
    }

    private fun setSwipeRefreshListener(selectedSection: UiSection) {
        with(viewBinding) {
            when (selectedSection) {
                UiSection.ONGOINGS -> swipeRefreshLayout.setOnRefreshListener {
                    dispatch(AnimeListView.UiEvent.UpdateOngoingsSection)
                    swipeRefreshLayout.isRefreshing = false
                }

                UiSection.ANNOUNCED -> swipeRefreshLayout.setOnRefreshListener {
                    dispatch(AnimeListView.UiEvent.UpdateAnnouncedSection)
                    swipeRefreshLayout.isRefreshing = false
                }

                UiSection.SEARCH -> swipeRefreshLayout.setOnRefreshListener {
                    dispatch(AnimeListView.UiEvent.UpdateSearchSection)
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun getSearch(uiModel: AnimeListView.UiModel): UiSearch {
        return uiModel.search
    }

    private fun setSearch(search: UiSearch) {
        with(viewBinding) {
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
        with(viewBinding) {
            when (contentType) {
                UiContentType.LOADED -> {
                    connectionStatusImage.isVisible = false
                    animeListRv.isVisible = true
                }

                UiContentType.LOADING -> {
                    animeListRv.isVisible = false
                    connectionStatusImage.setImageResource(R.drawable.loading_animation)
                    connectionStatusImage.isVisible = true
                }

                UiContentType.NO_DATA -> {
                    animeListRv.isVisible = false
                    connectionStatusImage.setImageResource(R.drawable.connection_error_48)
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
            setListItems(listItemsWithSelectedSection.listItems)
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
            setListItems(listItemsWithSelectedSection.listItems)
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
            setListItems(listItemsWithSelectedSection.listItems)
        }
    }

    private fun setListItems(listItems: List<UiListItem>) {
        adapter.submitList(listItems) {
            if (listItems.isNotEmpty()) {
                dispatch(AnimeListView.UiEvent.ContentTypeChange(UiContentType.LOADED))
                //TODO Убрать
                setContentType(UiContentType.LOADED)
                println("tagtag items: $listItems")
            } else {
                dispatch(AnimeListView.UiEvent.ContentTypeChange(UiContentType.NO_DATA))
                //TODO Убрать
                setContentType(UiContentType.NO_DATA)
            }
        }
    }

    data class ListItemsWithSelectedSection(
        val listItems: List<UiListItem>,
        val selectedSection: UiSection
    )
}
