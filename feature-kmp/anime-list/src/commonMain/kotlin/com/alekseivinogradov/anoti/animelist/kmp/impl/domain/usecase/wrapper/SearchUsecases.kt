package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.wrapper

import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnimeDetailsByIdUsecase
import com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase.FetchAnimeListBySearchUsecase

data class SearchUsecases(
    val fetchAnimeListBySearchUsecase: FetchAnimeListBySearchUsecase,
    val fetchAnimeDetailsByIdUsecase: FetchAnimeDetailsByIdUsecase
)
