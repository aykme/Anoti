package com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper

import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeByIdUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeOngoingListUsecase

data class OngoingUsecases(
    val fetchAnimeOngoingListUsecase: FetchAnimeOngoingListUsecase,
    val fetchAnimeByIdUsecase: FetchAnimeByIdUsecase
)
