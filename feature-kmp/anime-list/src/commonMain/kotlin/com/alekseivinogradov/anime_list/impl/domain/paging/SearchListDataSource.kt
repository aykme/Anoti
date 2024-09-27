package com.alekseivinogradov.anime_list.impl.domain.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.alekseivinogradov.anime_base.api.domain.FIRST_PAGING_PAGE
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeListBySearchUsecase
import com.alekseivinogradov.network.api.domain.model.CallResult

class SearchListDataSource(
    private val fetchAnimeListBySearchUsecase: FetchAnimeListBySearchUsecase,
    private val searchText: String
) : PagingSource<Int, ListItemDomain>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListItemDomain> {
        val page = params.key ?: FIRST_PAGING_PAGE

        val usecaseResult: CallResult<List<ListItemDomain>> = fetchAnimeListBySearchUsecase
            .execute(page = page, searchText = searchText)

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
