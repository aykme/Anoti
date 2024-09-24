package com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper

import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeByIdUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeListBySearchUsecase

data class SearchUsecases(
    val fetchAnimeListBySearchUsecase: FetchAnimeListBySearchUsecase,
    val fetchAnimeByIdUsecase: FetchAnimeByIdUsecase
)
