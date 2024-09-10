package com.alekseivinogradov.anime_list.api.domain.model.section

/**
 * @param id- id аниме в базе данных
 * @param name - название
 * @param imageUrl - ссылка на картинку
 * @param episodesInfoType - выбранный тип информации об эпизодах
 * @param episodesAired - количество показанных эпизодов
 * @param episodesTotal - всего эпизодов
 * @param extraEpisodesInfo - дата следующего эпизода / дата начала показа / дата завершения показа
 * @param score - оценка
 * @param releaseStatus - статус аниме (онгоинг, анонс, выпущено)
 */
data class ListItemDomain(
    val id: Int?,
    val name: String,
    val imageUrl: String?,
    val episodesInfoType: EpisodesInfoTypeDomain,
    val episodesAired: Int?,
    val episodesTotal: Int?,
    val extraEpisodesInfo: String?,
    val score: Float?,
    val releaseStatus: ReleaseStatusDomain
)