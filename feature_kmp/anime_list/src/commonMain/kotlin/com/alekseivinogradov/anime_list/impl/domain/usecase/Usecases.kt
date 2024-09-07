package com.alekseivinogradov.anime_list.impl.domain.usecase

data class Usecases(
    val fetchAnimeOngoingListUsecase: FetchAnimeOngoingListUsecase,
    val fetchAnimeAnnouncedListUsecase: FetchAnimeAnnouncedListUsecase,
    val fetchAnimeListBySearchUsecase: FetchAnimeListBySearchUsecase,
    val fetchAnimeByIdUsecase: FetchAnimeByIdUsecase
)