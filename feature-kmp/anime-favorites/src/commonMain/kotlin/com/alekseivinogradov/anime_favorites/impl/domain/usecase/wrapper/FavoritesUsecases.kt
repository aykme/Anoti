package com.alekseivinogradov.anime_favorites.impl.domain.usecase.wrapper

import com.alekseivinogradov.anime_favorites.impl.domain.usecase.FetchAnimeDetailsByIdUsecase

data class FavoritesUsecases(
    val fetchAnimeDetailsByIdUsecase: FetchAnimeDetailsByIdUsecase
)
