package com.alekseivinogradov.database.room.api.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alekseivinogradov.database.api.data.model.ReleaseStatusDb
import com.alekseivinogradov.database.room.api.data.animeTableName

@Entity(tableName = animeTableName)
data class AnimeDbPlatform(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "episodes_aired") val episodesAired: Int?,
    @ColumnInfo(name = "episodes_total") val episodesTotal: Int?,
    @ColumnInfo(name = "next_episode_at") val nextEpisodeAt: String?,
    @ColumnInfo(name = "aired_on") val airedOn: String?,
    @ColumnInfo(name = "released_on") val releasedOn: String?,
    @ColumnInfo(name = "score") val score: Float?,
    @ColumnInfo(name = "release_status") val releaseStatus: ReleaseStatusDb,
    @ColumnInfo(name = "episodes_viewed") val episodesViewed: Int,
    @ColumnInfo(name = "is_new_episode") val isNewEpisode: Boolean
)
