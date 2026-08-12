package com.alekseivinogradov.anoti.animelist.android.impl.presentation

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.alekseivinogradov.anoti.animebase.android.impl.presentation.adapter.decorator.BottomSpaceLastItemDecorator
import com.alekseivinogradov.anoti.animebase.android.impl.presentation.adapter.decorator.EdgeToEdgeItemDecorator
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.loading_in_progress
import com.alekseivinogradov.anoti.animelist.android.impl.presentation.adapter.AnimeListAdapter
import com.alekseivinogradov.anoti.animelist.kmp.R
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.AnimeListView
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.ListContentUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.SearchUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.SectionHatUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.UiModel
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.on_air
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.search_hint
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.search_off_description
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.search_on_description
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.soon
import com.alekseivinogradov.anoti.celebrity.android.impl.presentation.edgetoedge.isEdgeToEdgeEnabled
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.PAGING_PREFETCH_DISTANCE
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.connection_error
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.Res as baseRes
import com.alekseivinogradov.anoti.celebrity.kmp.R as res_R
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res as celebrityRes

// One function per bindable UI field/event, not incidental growth.
@Suppress("TooManyFunctions")
internal class AnimeListViewImpl(
    private val rootView: View,
    dateFormatter: DateFormatter,
    private val viewScope: CoroutineScope,
    private val coroutineContextProvider: CoroutineContextProvider
) : AnimeListView, BaseMviView<UiModel, AnimeListMainStore.Intent>() {

    private val context
        get() = rootView.context

    private val activeColor
        get() = context.getColor(res_R.color.cinnabar_500)

    private val defaultColor
        get() = context.getColor(res_R.color.white_transparent)

    private val onAirString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(Res.string.on_air)
        }
    private val soonString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(Res.string.soon)
        }
    private val searchOnDescriptionString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(Res.string.search_on_description)
        }
    private val searchOffDescriptionString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(Res.string.search_off_description)
        }
    private val searchHintString: String = runBlocking(coroutineContextProvider.ioDispatcher) {
        getString(Res.string.search_hint)
    }
    private val loadingInProgressString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(baseRes.string.loading_in_progress)
        }
    private val connectionErrorString: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(celebrityRes.string.connection_error)
        }

    private val swipeRefreshLayout: SwipeRefreshLayout =
        rootView.findViewById(R.id.swipe_refresh_layout)
    private val animeListLayout: View =
        rootView.findViewById(R.id.anime_list_layout)
    private val connectionStatusImage: AppCompatImageView =
        rootView.findViewById(R.id.connection_status_image)
    private val animeListRv: RecyclerView =
        rootView.findViewById(R.id.anime_list_rv)
    private val upperMenuLayout: View =
        rootView.findViewById(R.id.upper_menu_layout)
    private val ongoingButton: MaterialButton =
        rootView.findViewById(R.id.ongoing_button)
    private val verticalDivider: View =
        rootView.findViewById(R.id.vertical_divider)
    private val announcedButton: MaterialButton =
        rootView.findViewById(R.id.announced_button)
    private val searchButton: AppCompatImageView =
        rootView.findViewById(R.id.search_button)
    private val searchButtonShadow: AppCompatImageView =
        rootView.findViewById(R.id.search_button_shadow)
    private val searchInputLayout: TextInputLayout =
        rootView.findViewById(R.id.search_input_layout)
    private val searchEditText: TextInputEditText =
        rootView.findViewById(R.id.search_edit_text)
    private val searchCancelButton: AppCompatImageView =
        rootView.findViewById(R.id.search_cancel_button)

    private val adapter = AnimeListAdapter(
        episodesInfoClickAdapterCallback = ::episodesInfoClickAdapterCallback,
        notificationClickAdapterCallback = ::notificationClickAdapterCallback,
        dateFormatter = dateFormatter,
        coroutineContextProvider = coroutineContextProvider
    )

    private var itemDecorator: EdgeToEdgeItemDecorator? = null

    private val loadNextPageScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy <= 0) return
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
            val totalItemCount = layoutManager.itemCount
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            if (lastVisibleItemPosition >= totalItemCount - PAGING_PREFETCH_DISTANCE) {
                dispatch(AnimeListMainStore.Intent.LoadNextPage)
            }
        }
    }

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

    private fun episodesInfoClickAdapterCallback(id: AnimeId) {
        dispatch(AnimeListMainStore.Intent.EpisodesInfoClick(id))
    }

    private fun notificationClickAdapterCallback(id: AnimeId) {
        dispatch(AnimeListMainStore.Intent.NotificationClick(id))
    }

    private fun initEdgeToEdgeListenerIfNeeded() {
        if (isEdgeToEdgeEnabled()) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                upperMenuLayout.setPadding(
                    /* left = */
                    upperMenuLayout.paddingLeft,
                    /* top = */
                    systemBars.top,
                    /* right = */
                    upperMenuLayout.paddingRight,
                    /* bottom = */
                    upperMenuLayout.paddingBottom
                )

                itemDecorator?.let { oldItemDecorator: EdgeToEdgeItemDecorator ->
                    animeListRv.removeItemDecoration(oldItemDecorator)
                }
                itemDecorator = EdgeToEdgeItemDecorator(systemBarTopOffset = systemBars.top)
                itemDecorator?.let { newItemDecorator: EdgeToEdgeItemDecorator ->
                    animeListRv.addItemDecoration(newItemDecorator)
                }

                insets
            }
        }
    }

    private fun initSwipeToRefresh() {
        swipeRefreshLayout.setProgressViewOffset(
            /* scale = */
            false,
            /* start = */
            com.alekseivinogradov.anoti.celebrity.kmp.api.domain.SWIPE_REFRESH_START_OFFSET,
            /* end = */
            com.alekseivinogradov.anoti.celebrity.kmp.api.domain.SWIPE_REFRESH_END_OFFSET
        )
        swipeRefreshLayout.setColorSchemeResources(res_R.color.cinnabar_500)
        swipeRefreshLayout.setOnRefreshListener {
            dispatch(AnimeListMainStore.Intent.UpdateSection)
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun initCommonFields() {
        swipeRefreshLayout.isVisible = true
        animeListLayout.isVisible = true
        upperMenuLayout.isVisible = true
        ongoingButton.text = onAirString
        announcedButton.text = soonString
        searchButton.contentDescription = searchOnDescriptionString
        searchCancelButton.contentDescription = searchOffDescriptionString
        searchInputLayout.hint = searchHintString
    }

    private fun initClickListeners() {
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

    private fun initSearchTextChangedListener() {
        searchEditText.addTextChangedListener(
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
        animeListRv.adapter = adapter
        animeListRv.layoutManager = LinearLayoutManager(
            /* context = */
            context,
            /* orientation = */
            LinearLayoutManager.VERTICAL,
            /* reverseLayout = */
            false
        )
        animeListRv.itemAnimator = null
        animeListRv.addItemDecoration(BottomSpaceLastItemDecorator())
        animeListRv.addOnScrollListener(loadNextPageScrollListener)
    }

    private fun getSelectedSection(uiModel: UiModel): SectionHatUi {
        return uiModel.selectedSection
    }

    private fun setSelectedSection(selectedSection: SectionHatUi) {
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

    private fun getSearch(uiModel: UiModel): SearchUi {
        return uiModel.search
    }

    private fun setSearch(search: SearchUi) {
        when (search) {
            SearchUi.HIDDEN -> {
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

    private fun hideKeyboard() {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(rootView.windowToken, 0)
    }

    private fun getContentType(uiModel: UiModel): ContentTypeUi {
        return uiModel.contentType
    }

    private var contentTypeChangeJob: Job? = null
    private fun setContentType(contentType: ContentTypeUi) {
        contentTypeChangeJob?.cancel()
        contentTypeChangeJob = viewScope.launch(coroutineContextProvider.mainCoroutineContext) {
            when (contentType) {
                ContentTypeUi.LOADED -> {
                    connectionStatusImage.isVisible = false
                    animeListRv.isVisible = true
                }

                ContentTypeUi.LOADING -> {
                    animeListRv.isVisible = false
                    connectionStatusImage.setImageResource(res_R.drawable.loading_animation)
                    connectionStatusImage.contentDescription = loadingInProgressString
                    connectionStatusImage.isVisible = true
                }

                ContentTypeUi.ERROR -> {
                    animeListRv.isVisible = false
                    connectionStatusImage.setImageResource(R.drawable.connection_error_48)
                    connectionStatusImage.contentDescription = connectionErrorString
                    connectionStatusImage.isVisible = true
                }
            }
        }
    }

    private fun getListContent(uiModel: UiModel): ListContentUi {
        return uiModel.listContent
    }

    private var submitListJob: Job? = null
    private fun setListContent(listContent: ListContentUi) {
        submitListJob?.cancel()
        submitListJob = viewScope.launch(coroutineContextProvider.mainCoroutineContext) {
            if (listContent.isNeedToResetListPositon) {
                animeListRv.stopScroll()
            }
            adapter.submitList(listContent.listItems) {
                if (listContent.isNeedToResetListPositon) {
                    animeListRv.scrollToPosition(0)
                    dispatch(
                        AnimeListMainStore.Intent.ChangeResetListPositionFlag(
                            isNeedToResetListPosition = false
                        )
                    )
                }
            }
        }
    }
}
