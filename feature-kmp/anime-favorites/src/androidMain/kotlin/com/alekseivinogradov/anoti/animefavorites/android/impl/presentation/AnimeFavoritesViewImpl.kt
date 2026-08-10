package com.alekseivinogradov.anoti.animefavorites.android.impl.presentation

import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.alekseivinogradov.anoti.animebase.android.impl.presentation.adapter.decorator.BottomSpaceLastItemDecorator
import com.alekseivinogradov.anoti.animebase.android.impl.presentation.adapter.decorator.EdgeToEdgeItemDecorator
import com.alekseivinogradov.anoti.animefavorites.android.impl.presentation.adapter.AnimeFavoritesAdapter
import com.alekseivinogradov.anoti.animefavorites.kmp.R
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.AnimeFavoritesView
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.UiModel
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ListItemUi
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.SWIPE_REFRESH_END_OFFSET
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.SWIPE_REFRESH_START_OFFSET
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.android.impl.presentation.edgetoedge.isEdgeToEdgeEnabled
import com.alekseivinogradov.anoti.celebrity.kmp.R as res_R
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView

internal class AnimeFavoritesViewImpl(
    private val rootView: View,
    dateFormatter: DateFormatter
) : AnimeFavoritesView, BaseMviView<UiModel, AnimeFavoritesMainStore.Intent>() {

    private val context
        get() = rootView.context

    private val swipeRefreshLayout: SwipeRefreshLayout =
        rootView.findViewById(R.id.swipe_refresh_layout)
    private val animeFavoritesLayout: FrameLayout = rootView.findViewById(R.id.anime_favorites_layout)
    private val connectionStatusImage: AppCompatImageView =
        rootView.findViewById(R.id.connection_status_image)
    private val animeFavoritesEmptyLayout: ConstraintLayout =
        rootView.findViewById(R.id.anime_favorites_empty_container)
    private val emptyMainImage: ShapeableImageView =
        animeFavoritesEmptyLayout.findViewById(R.id.main_image)
    private val emptyMainInfoText: MaterialTextView =
        animeFavoritesEmptyLayout.findViewById(R.id.main_info_text)
    private val animeFavoritesRv: RecyclerView = rootView.findViewById(R.id.anime_favorites_rv)

    private val adapter = AnimeFavoritesAdapter(
        itemClickAdapterCallback = ::itemClickAdapterCallback,
        infoTypeClickAdapterCallback = ::infoTypeClickAdapterCallback,
        notificationClickAdapterCallback = ::notificationClickAdapterCallback,
        episodesViewedMinusClickAdapterCallback = ::episodesViewedMinusClickAdapterCallback,
        episodesViewedPlusClickAdapterCallback = ::episodesViewedPlusClickAdapterCallback,
        dateFormatter = dateFormatter
    )

    private var itemDecorator: EdgeToEdgeItemDecorator? = null

    init {
        initEdgeToEdgeListenerIfNeeded()
        initSwipeToRefresh()
        initCommonFields()
        initRv()
    }

    override val renderer: ViewRenderer<UiModel> = diff {
        diff(
            get = ::getContentType,
            set = ::setContentType
        )
        diff(
            get = ::getListItems,
            set = ::setListItems
        )
    }

    private fun initEdgeToEdgeListenerIfNeeded() {
        if (isEdgeToEdgeEnabled()) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView)
            { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                animeFavoritesEmptyLayout.setPadding(
                    /* left = */animeFavoritesEmptyLayout.paddingLeft,
                    /* top = */systemBars.top,
                    /* right = */animeFavoritesEmptyLayout.paddingRight,
                    /* bottom = */animeFavoritesEmptyLayout.paddingBottom
                )

                itemDecorator?.let { oldItemDecorator: EdgeToEdgeItemDecorator ->
                    animeFavoritesRv.removeItemDecoration(oldItemDecorator)
                }
                itemDecorator = EdgeToEdgeItemDecorator(systemBarTopOffset = systemBars.top)
                itemDecorator?.let { newItemDecorator: EdgeToEdgeItemDecorator ->
                    animeFavoritesRv.addItemDecoration(newItemDecorator)
                }

                insets
            }
        }
    }

    private fun initSwipeToRefresh() {
        swipeRefreshLayout.setProgressViewOffset(
            /* scale = */ false,
            /* start = */ SWIPE_REFRESH_START_OFFSET,
            /* end = */ SWIPE_REFRESH_END_OFFSET
        )
        swipeRefreshLayout.setColorSchemeResources(res_R.color.cinnabar_500)
        swipeRefreshLayout.setOnRefreshListener {
            dispatch(AnimeFavoritesMainStore.Intent.UpdateSection)
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun initCommonFields() {
        swipeRefreshLayout.isVisible = true
        animeFavoritesLayout.isVisible = true
        emptyMainImage.contentDescription = context
            .getString(R.string.empty_list_image_description)
        emptyMainInfoText.text = context.getString(R.string.empty_list)
    }

    private fun initRv() {
        animeFavoritesRv.adapter = adapter
        animeFavoritesRv.layoutManager = LinearLayoutManager(
            /* context = */ context,
            /* orientation = */ LinearLayoutManager.VERTICAL,
            /* reverseLayout = */ false
        )
        animeFavoritesRv.addItemDecoration(BottomSpaceLastItemDecorator())
    }

    private fun getContentType(uiModel: UiModel): ContentTypeUi {
        return uiModel.contentType
    }

    private fun setContentType(contentType: ContentTypeUi) {
        when (contentType) {
            ContentTypeUi.LOADED -> {
                animeFavoritesEmptyLayout.isVisible = false
                connectionStatusImage.isVisible = false
                swipeRefreshLayout.isEnabled = true
                animeFavoritesRv.isVisible = true
            }

            ContentTypeUi.LOADING -> {
                animeFavoritesEmptyLayout.isVisible = false
                animeFavoritesRv.isVisible = false
                swipeRefreshLayout.isEnabled = false
                connectionStatusImage.isVisible = true
            }

            ContentTypeUi.EMPTY -> {
                animeFavoritesRv.isVisible = false
                swipeRefreshLayout.isEnabled = false
                connectionStatusImage.isVisible = false
                animeFavoritesEmptyLayout.isVisible = true
            }
        }
    }

    private fun getListItems(uiModel: UiModel): List<ListItemUi> {
        return uiModel.listItems
    }

    private fun setListItems(listItems: List<ListItemUi>) {
        adapter.submitList(listItems) {
            if (listItems.isNotEmpty()) {
                dispatch(AnimeFavoritesMainStore.Intent.ItemsSubmittedToList)
            }
        }
    }

    private fun itemClickAdapterCallback(id: AnimeId) {
        dispatch(AnimeFavoritesMainStore.Intent.ItemClick(id))
    }

    private fun infoTypeClickAdapterCallback(id: AnimeId) {
        dispatch(AnimeFavoritesMainStore.Intent.InfoTypeClick(id))
    }

    private fun notificationClickAdapterCallback(id: AnimeId) {
        dispatch(AnimeFavoritesMainStore.Intent.NotificationClick(id))
    }

    private fun episodesViewedMinusClickAdapterCallback(id: AnimeId) {
        dispatch(AnimeFavoritesMainStore.Intent.EpisodesViewedMinusClick(id))
    }

    private fun episodesViewedPlusClickAdapterCallback(id: AnimeId) {
        dispatch(AnimeFavoritesMainStore.Intent.EpisodesViewedPlusClick(id))
    }
}
