package com.alekseivinogradov.anime_list.presentation

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.alekseivinogradov.anime_list.R
import com.alekseivinogradov.anime_list.api.presentation.AnimeListView
import com.alekseivinogradov.anime_list.api.presentation.mapper.model.UiModel
import com.alekseivinogradov.anime_list.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anime_list.api.presentation.model.SearchUi
import com.alekseivinogradov.anime_list.api.presentation.model.SectionUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.ListItemUi
import com.alekseivinogradov.anime_list.databinding.FragmentAnimeListBinding
import com.alekseivinogradov.anime_list.presentation.adapter.AnimeListAdapter
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import com.alekseivinogradov.theme.R as theme_R

internal class AnimeListViewImpl(
    private val viewBinding: FragmentAnimeListBinding
) : AnimeListView, BaseMviView<UiModel, AnimeListView.UiEvent>() {

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

    override val renderer: ViewRenderer<UiModel> = diff {
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

    private fun getSelectedSection(uiModel: UiModel): SectionUi {
        return uiModel.selectedSection
    }

    private fun setSelectedSection(selectedSection: SectionUi) {
        with(viewBinding) {
            when (selectedSection) {
                SectionUi.ONGOINGS -> {
                    ongoingButton.setTextColor(activeColor)
                    announcedButton.setTextColor(defaultColor)
                    searchButton.setColorFilter(defaultColor)
                }

                SectionUi.ANNOUNCED -> {
                    announcedButton.setTextColor(activeColor)
                    ongoingButton.setTextColor(defaultColor)
                    searchButton.setColorFilter(defaultColor)
                }

                SectionUi.SEARCH -> {
                    searchButton.setColorFilter(activeColor)
                    announcedButton.setTextColor(defaultColor)
                    ongoingButton.setTextColor(defaultColor)
                }
            }
        }
        setSwipeRefreshListener(selectedSection)
    }

    private fun setSwipeRefreshListener(selectedSection: SectionUi) {
        with(viewBinding) {
            swipeRefreshLayout.setProgressViewOffset(false, 45, 245)
            swipeRefreshLayout.setColorSchemeResources(theme_R.color.pink)
            when (selectedSection) {
                SectionUi.ONGOINGS -> swipeRefreshLayout.setOnRefreshListener {
                    dispatch(AnimeListView.UiEvent.UpdateOngoingsSection)
                    swipeRefreshLayout.isRefreshing = false
                }

                SectionUi.ANNOUNCED -> swipeRefreshLayout.setOnRefreshListener {
                    dispatch(AnimeListView.UiEvent.UpdateAnnouncedSection)
                    swipeRefreshLayout.isRefreshing = false
                }

                SectionUi.SEARCH -> swipeRefreshLayout.setOnRefreshListener {
                    dispatch(AnimeListView.UiEvent.UpdateSearchSection)
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun getSearch(uiModel: UiModel): SearchUi {
        return uiModel.search
    }

    private fun setSearch(search: SearchUi) {
        with(viewBinding) {
            when (search) {
                SearchUi.HIDEN -> {
                    hideKeyboard()
                    searchInputLayout.isVisible = false
                    searchCancelButton.isVisible = false
                    ongoingButton.isVisible = true
                    verticalDivider.isVisible = true
                    announcedButton.isVisible = true
                    searchButton.isVisible = true
                    searchButtonShadow.isVisible = true
                }

                SearchUi.SHOWN -> {
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

    private fun hideKeyboard() {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(viewBinding.root.windowToken, 0)
    }

    private fun getContentType(uiModel: UiModel): ContentTypeUi {
        return uiModel.contentType
    }

    private fun setContentType(contentType: ContentTypeUi) {
        with(viewBinding) {
            when (contentType) {
                ContentTypeUi.LOADED -> {
                    connectionStatusImage.isVisible = false
                    animeListRv.isVisible = true
                }

                ContentTypeUi.LOADING -> {
                    animeListRv.isVisible = false
                    connectionStatusImage.setImageResource(R.drawable.loading_animation)
                    connectionStatusImage.isVisible = true
                }

                ContentTypeUi.NO_DATA -> {
                    animeListRv.isVisible = false
                    connectionStatusImage.setImageResource(R.drawable.connection_error_48)
                    connectionStatusImage.isVisible = true
                }
            }
        }
    }

    private fun getOngoingListItems(uiModel: UiModel): ListItemsWithSelectedSection {
        return ListItemsWithSelectedSection(
            listItems = uiModel.ongoingListItems,
            selectedSection = uiModel.selectedSection
        )
    }

    private fun setOngoingListItems(listItemsWithSelectedSection: ListItemsWithSelectedSection) {
        if (listItemsWithSelectedSection.selectedSection == SectionUi.ONGOINGS) {
            setListItems(listItemsWithSelectedSection.listItems)
        }
    }

    private fun getAnnouncedListItems(
        uiModel: UiModel
    ): ListItemsWithSelectedSection {
        return ListItemsWithSelectedSection(
            listItems = uiModel.announcedListItems,
            selectedSection = uiModel.selectedSection
        )
    }

    private fun setAnnouncedListItems(listItemsWithSelectedSection: ListItemsWithSelectedSection) {
        if (listItemsWithSelectedSection.selectedSection == SectionUi.ANNOUNCED) {
            setListItems(listItemsWithSelectedSection.listItems)
        }
    }

    private fun getSearchListItems(uiModel: UiModel): ListItemsWithSelectedSection {
        return ListItemsWithSelectedSection(
            listItems = uiModel.searchListItems,
            selectedSection = uiModel.selectedSection
        )
    }

    private fun setSearchListItems(listItemsWithSelectedSection: ListItemsWithSelectedSection) {
        if (listItemsWithSelectedSection.selectedSection == SectionUi.SEARCH) {
            setListItems(listItemsWithSelectedSection.listItems)
        }
    }

    private fun setListItems(listItems: List<ListItemUi>) {
        adapter.submitList(listItems)
    }

    data class ListItemsWithSelectedSection(
        val listItems: List<ListItemUi>,
        val selectedSection: SectionUi
    )
}
