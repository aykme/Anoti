package com.alekseivinogradov.anoti.animedatabase.kmp.impl.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.model.AnimeDbEntity
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(anime: AnimeDbEntity)

    @Update
    suspend fun update(anime: AnimeDbEntity)

    @Query("SELECT * FROM $animeTableName ORDER BY release_status DESC, name ASC")
    fun getAllItemsFlow(): Flow<List<AnimeDbEntity>>

    @Query("SELECT * FROM $animeTableName ORDER BY release_status DESC, name ASC")
    suspend fun getAllItems(): List<AnimeDbEntity>

    @Query("DELETE FROM $animeTableName WHERE id =:id")
    suspend fun delete(id: AnimeId)

    /**
     * No using Boolean params in "Query",
     * otherwise there will be a crash on older versions of android
     */
    @Query("UPDATE $animeTableName SET is_new_episode = 0")
    suspend fun resetAllItemsNewEpisodeStatus()

    @Query("UPDATE $animeTableName SET is_new_episode = :isNewEpisode WHERE id =:id")
    suspend fun changeItemNewEpisodeStatus(id: AnimeId, isNewEpisode: Boolean)
}
