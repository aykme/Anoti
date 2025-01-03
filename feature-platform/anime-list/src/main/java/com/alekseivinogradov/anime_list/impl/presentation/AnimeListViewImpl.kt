package com.alekseivinogradov.anime_list.impl.presentation

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.alekseivinogradov.anime_base.impl.presentation.adapter.decorator.BottomSpaceLastItemDecorator
import com.alekseivinogradov.anime_base.impl.presentation.adapter.decorator.EdgeToEdgeItemDecorator
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
import com.alekseivinogradov.celebrity.api.domain.PAGING_SUBMIT_LIST_DELAY_MILLISECONDS
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.celebrity.api.domain.formatter.DateFormatter
import com.alekseivinogradov.celebrity.impl.presentation.edge_to_edge.isEdgeToEdgeEnabled
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.alekseivinogradov.res.R as res_R

internal class AnimeListViewImpl(
    private val viewBinding: FragmentAnimeListBinding,
    dateFormatter: DateFormatter,
    private val viewScope: CoroutineScope,
    private val coroutineContextProvider: CoroutineContextProvider
) : AnimeListView, BaseMviView<UiModel, AnimeListMainStore.Intent>() {

    private val context
        get() = viewBinding.root.context

    private val activeColor
        get() = context.getColor(res_R.color.cinnabar_500)

    private val defaultColor
        get() = context.getColor(res_R.color.white_transparent)

    private val resetListPositionCallback: () -> Unit = {
        viewBinding.animeListRv.scrollToPosition(0)
        dispatch(
            AnimeListMainStore.Intent.ChangeResetListPositionFlag(
                isNeedToResetListPosition = false
            )
        )
    }

    private val adapter = AnimeListAdapter(
        episodesInfoClickAdapterCallback = ::episodesInfoClickAdapterCallback,
        notificationClickAdapterCallback = ::notificationClickAdapterCallback,
        dateFormatter = dateFormatter
    )

    private var itemDecorator: EdgeToEdgeItemDecorator? = null

    init {
        initEdgeToEdgeListenerIfNeeded()
        initSwipeToRefresh()
        initCommonFields()
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

    private fun initEdgeToEdgeListenerIfNeeded() {
        if (isEdgeToEdgeEnabled()) {
            ViewCompat.setOnApplyWindowInsetsListener(viewBinding.root)
            { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                val upperMenu = viewBinding.upperMenuLayout
                upperMenu.setPadding(
                    /* left = */upperMenu.paddingLeft,
                    /* top = */systemBars.top,
                    /* right = */upperMenu.paddingRight,
                    /* bottom = */upperMenu.paddingBottom
                )

                itemDecorator?.let { oldItemDecorator: EdgeToEdgeItemDecorator ->
                    viewBinding.animeListRv.removeItemDecoration(oldItemDecorator)
                }
                itemDecorator = EdgeToEdgeItemDecorator(systemBarTopOffset = systemBars.top)
                itemDecorator?.let { newItemDecorator: EdgeToEdgeItemDecorator ->
                    viewBinding.animeListRv.addItemDecoration(newItemDecorator)
                }

                insets
            }
        }
    }

    private fun initSwipeToRefresh() {
        with(viewBinding) {
            swipeRefreshLayout.setProgressViewOffset(
                /* scale = */ false,
                /* start = */ com.alekseivinogradov.celebrity.api.domain.SWIPE_REFRESH_START_OFFSET,
                /* end = */ com.alekseivinogradov.celebrity.api.domain.SWIPE_REFRESH_END_OFFSET
            )
            swipeRefreshLayout.setColorSchemeResources(res_R.color.cinnabar_500)
            swipeRefreshLayout.setOnRefreshListener {
                dispatch(AnimeListMainStore.Intent.UpdateSection)
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun initCommonFields() {
        with(viewBinding) {
            swipeRefreshLayout.isVisible = true
            animeListLayout.isVisible = true
            upperMenuLayout.isVisible = true
            ongoingButton.text = context.getString(R.string.on_air)
            announcedButton.text = context.getString(R.string.soon)
            searchButton.contentDescription = context.getString(R.string.search_on_description)
            searchCancelButton.contentDescription = context
                .getString(R.string.search_off_description)
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
            animeListRv.addItemDecoration(BottomSpaceLastItemDecorator())
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
        contentTypeChangeJob = viewScope.launch(coroutineContextProvider.mainCoroutineContext) {
            with(viewBinding) {
                when (contentType) {
                    ContentTypeUi.LOADED -> {
                        connectionStatusImage.isVisible = false
                        animeListRv.isVisible = true
                    }

                    ContentTypeUi.LOADING -> {
                        animeListRv.isVisible = false
                        connectionStatusImage.setImageResource(res_R.drawable.loading_animation)
                        connectionStatusImage.contentDescription = context
                            .getString(R.string.loading_in_progress)
                        connectionStatusImage.isVisible = true
                    }

                    ContentTypeUi.ERROR -> {
                        animeListRv.isVisible = false
                        connectionStatusImage.setImageResource(R.drawable.connection_error_48)
                        connectionStatusImage.contentDescription = context
                            .getString(R.string.connection_error)
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
        submitDataJob = viewScope.launch(coroutineContextProvider.mainCoroutineContext) {
            /**
             * The reason for this delay is that if the list is updated within a few milliseconds,
             * the PagingDataAdapter freezes and does not update the items.
             * Apparently, this is a library bug.
             * This is a big problem in MVI, as state can be updated very often.
             */
            delay(PAGING_SUBMIT_LIST_DELAY_MILLISECONDS)
            adapter.removeOnPagesUpdatedListener(resetListPositionCallback)
            if (listContent.isNeedToResetListPositon) {
                viewBinding.animeListRv.stopScroll()
                adapter.addOnPagesUpdatedListener(resetListPositionCallback)
            }
            adapter.submitData(listContent.listItems)
        }
    }
}
