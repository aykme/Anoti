package com.alekseivinogradov.anime_list.impl.presentation

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.alekseivinogradov.anime_base.api.domain.PAGING_SUBMIT_LIST_DELAY
import com.alekseivinogradov.anime_base.api.domain.SWIPE_REFRESH_END_OFFSET
import com.alekseivinogradov.anime_base.api.domain.SWIPE_REFRESH_START_OFFSET
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anime_list.api.presentation.AnimeListView
import com.alekseivinogradov.anime_list.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anime_list.api.presentation.model.ListContentUi
import com.alekseivinogradov.anime_list.api.presentation.model.SearchUi
import com.alekseivinogradov.anime_list.api.presentation.model.SectionHatUi
import com.alekseivinogradov.anime_list.api.presentation.model.UiModel
import com.alekseivinogradov.anime_list.impl.presentation.adapter.AnimeListAdapter
import com.alekseivinogradov.anime_list_platform.R
import com.alekseivinogradov.anime_list_platform.databinding.FragmentAnimeListBinding
import com.alekseivinogradov.date.formatter.DateFormatter
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.alekseivinogradov.atom.R as atom_R
import com.alekseivinogradov.theme.R as theme_R

internal class AnimeListViewImpl(
    private val viewBinding: FragmentAnimeListBinding,
    dateFormatter: DateFormatter,
    private val viewScope: CoroutineScope
) : AnimeListView, BaseMviView<UiModel, AnimeListMainStore.Intent>() {

    private val context
        get() = viewBinding.root.context

    private val activeColor
        get() = context.getColor(theme_R.color.pink)

    private val defaultColor
        get() = context.getColor(theme_R.color.white_transparent)

    private val resetListPositionCallback: () -> Unit = {
        viewBinding.animeListRv.scrollToPosition(0)
        dispatch(
            AnimeListMainStore.Intent.ChangeResetListPositionAfterUpdateStatus(
                isNeedToResetListPosition = false
            )
        )
    }

    private val adapter = AnimeListAdapter(
        episodesInfoClickAdapterCallback = ::episodesInfoClickAdapterCallback,
        notificationClickAdapterCallback = ::notificationClickAdapterCallback,
        dateFormatter = dateFormatter
    )

    init {
        setSwipeToRefresh()
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
            get = ::getListContent,
            set = ::setListContent
        )
    }

    private fun episodesInfoClickAdapterCallback(listItem: ListItemDomain) {
        dispatch(AnimeListMainStore.Intent.EpisodesInfoClick(listItem))
    }

    private fun notificationClickAdapterCallback(listItem: ListItemDomain) {
        dispatch(AnimeListMainStore.Intent.NotificationClick(listItem))
    }

    private fun setSwipeToRefresh() {
        with(viewBinding) {
            swipeRefreshLayout.setProgressViewOffset(
                /* scale = */ false,
                /* start = */ SWIPE_REFRESH_START_OFFSET,
                /* end = */ SWIPE_REFRESH_END_OFFSET
            )
            swipeRefreshLayout.setColorSchemeResources(theme_R.color.pink)
            swipeRefreshLayout.setOnRefreshListener {
                dispatch(AnimeListMainStore.Intent.UpdateSection)
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun setCommonFields() {
        with(viewBinding) {
            swipeRefreshLayout.isVisible = true
            animeListLayout.isVisible = true
            ongoingButton.text = context.getString(R.string.ongoings)
            announcedButton.text = context.getString(R.string.announced)
            searchInputLayout.hint = context.getString(R.string.search_hint)
        }
    }

    private fun initClickListeners() {
        with(viewBinding) {
            ongoingButton.setOnClickListener {
                dispatch(AnimeListMainStore.Intent.OngoingsSectionClick)
            }
            announcedButton.setOnClickListener {
                dispatch(AnimeListMainStore.Intent.AnnouncedSectionClick)
            }
            searchButton.setOnClickListener {
                dispatch(AnimeListMainStore.Intent.SearchSectionClick)
            }
            searchCancelButton.setOnClickListener {
                dispatch(AnimeListMainStore.Intent.CancelSearchClick)
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
                    dispatch(AnimeListMainStore.Intent.ChangeSearchText(s?.toString().orEmpty()))
                }
            }
        )
    }

    private fun initRv() {
        with(viewBinding) {
            animeListRv.adapter = adapter
            animeListRv.layoutManager = LinearLayoutManager(
                /* context = */ context,
                /* orientation = */ LinearLayoutManager.VERTICAL,
                /* reverseLayout = */ false
            )
            animeListRv.itemAnimator = null
        }
    }

    private fun getSelectedSection(uiModel: UiModel): SectionHatUi {
        return uiModel.selectedSection
    }

    private fun setSelectedSection(selectedSection: SectionHatUi) {
        with(viewBinding) {
            when (selectedSection) {
                SectionHatUi.ONGOINGS -> {
                    ongoingButton.setTextColor(activeColor)
                    announcedButton.setTextColor(defaultColor)
                    searchButton.setColorFilter(defaultColor)
                }

                SectionHatUi.ANNOUNCED -> {
                    announcedButton.setTextColor(activeColor)
                    ongoingButton.setTextColor(defaultColor)
                    searchButton.setColorFilter(defaultColor)
                }

                SectionHatUi.SEARCH -> {
                    searchButton.setColorFilter(activeColor)
                    announcedButton.setTextColor(defaultColor)
                    ongoingButton.setTextColor(defaultColor)
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

    private var contentTypeChangeJob: Job? = null
    private fun setContentType(contentType: ContentTypeUi) {
        contentTypeChangeJob?.cancel()
        contentTypeChangeJob = viewScope.launch {
            with(viewBinding) {
                when (contentType) {
                    ContentTypeUi.LOADED -> {
                        /**
                         * The reason for this delay is so that
                         * the list can be updated before the ContentType change
                         */
                        delay(PAGING_SUBMIT_LIST_DELAY * 4)
                        connectionStatusImage.isVisible = false
                        animeListRv.isVisible = true
                    }

                    ContentTypeUi.LOADING -> {
                        animeListRv.isVisible = false
                        connectionStatusImage.setImageResource(atom_R.drawable.loading_animation)
                        connectionStatusImage.isVisible = true
                    }

                    ContentTypeUi.ERROR -> {
                        animeListRv.isVisible = false
                        connectionStatusImage.setImageResource(R.drawable.connection_error_48)
                        connectionStatusImage.isVisible = true
                    }
                }
            }
        }
    }

    private fun getListContent(uiModel: UiModel): ListContentUi {
        return uiModel.listContent
    }

    private var submitDataJob: Job? = null
    private fun setListContent(listContent: ListContentUi) {
        submitDataJob?.cancel()
        submitDataJob = viewScope.launch {
            /**
             * The reason for this delay is that if the list is updated within a few milliseconds,
             * the PagingDataAdapter freezes and does not update the items.
             * Apparently, this is a library bug.
             * This is a big problem in MVI, as state can be updated very often.
             */
            delay(PAGING_SUBMIT_LIST_DELAY)
            adapter.removeOnPagesUpdatedListener(resetListPositionCallback)
            if (listContent.isNeedToResetListPositon) {
                adapter.addOnPagesUpdatedListener(resetListPositionCallback)
            }
            adapter.submitData(listContent.listItems)
        }
    }
}
