package com.alekseivinogradov.anime_list.impl.domain.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.alekseivinogradov.anime_base.api.domain.FIRST_PAGING_PAGE
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchOngoingAnimeListUsecase
import com.alekseivinogradov.network.api.domain.model.CallResult

class OngoingListDataSource(
    private val fetchOngoingAnimeListUseCase: FetchOngoingAnimeListUsecase
) : PagingSource<Int, ListItemDomain>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListItemDomain> {
        val page = params.key ?: FIRST_PAGING_PAGE

        val usecaseResult: CallResult<List<ListItemDomain>> = fetchOngoingAnimeListUseCase
            .execute(page = page)

        return when (usecaseResult) {
            is CallResult.Success -> LoadResult.Page(
                data = usecaseResult.value,
                prevKey = if (page <= FIRST_PAGING_PAGE) null else (page - 1),
                nextKey = page + 1
            )

            is CallResult.HttpError -> LoadResult.Error(usecaseResult.throwable)
            is CallResult.OtherError -> LoadResult.Error(usecaseResult.throwable)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListItemDomain>): Int? {
        return state.anchorPosition?.let { anchorPosition: Int ->
            state.closestPageToPosition(anchorPosition)?.prevKey
        }
    }
}
