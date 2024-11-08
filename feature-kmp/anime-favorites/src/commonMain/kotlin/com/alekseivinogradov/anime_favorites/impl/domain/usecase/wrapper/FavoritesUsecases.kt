package com.alekseivinogradov.anime_favorites.impl.domain.usecase.wrapper

import com.alekseivinogradov.anime_favorites.api.domain.usecase.UpdateAllAnimeInBackgroundUsecase
import com.alekseivinogradov.anime_favorites.impl.domain.usecase.FetchAnimeDetailsByIdUsecase

data class FavoritesUsecases(
    val updateAllAnimeInBackgroundUsecase: UpdateAllAnimeInBackgroundUsecase,
    val fetchAnimeDetailsByIdUsecase: FetchAnimeDetailsByIdUsecase
)
