package com.alekseivinogradov.database.room.api.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.database.room.api.data.model.AnimeDbPlatform
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(anime: AnimeDbPlatform)

    @Update
    suspend fun update(anime: AnimeDbPlatform)

    @Query("SELECT * FROM anime_table ORDER BY release_status DESC, name ASC")
    fun getAllItemsFlow(): Flow<List<AnimeDbPlatform>>

    @Query("SELECT * FROM anime_table ORDER BY release_status DESC, name ASC")
    suspend fun getAllItems(): List<AnimeDbPlatform>

    @Query("DELETE FROM anime_table WHERE id =:id")
    suspend fun delete(id: AnimeId)

    @Query("UPDATE anime_table SET is_new_episode = false")
    suspend fun resetAllItemsNewEpisodeStatus()

    @Query("UPDATE anime_table SET is_new_episode = :isNewEpisode WHERE id =:id")
    suspend fun changeItemNewEpisodeStatus(id: AnimeId, isNewEpisode: Boolean)
}
