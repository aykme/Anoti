package com.alekseivinogradov.anoti.animedatabase.kmp.impl.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class AnimeDatabaseContinuityTest {

    @Test
    fun opensAnExistingPreMigrationDatabaseFileWithoutWipingItsData() = runTest {
        val dbPath = "anoti_anime_table_continuity_test.db"
        seedLegacyDatabaseFile(dbPath)

        val database = getRoomDatabase(
            Room.databaseBuilder<AnimeDatabase>(name = dbPath)
        )
        val items = database.animeDao().getAllItems()
        database.close()

        assertEquals(1, items.size)
        val item = items.first()
        assertEquals(42, item.id)
        assertEquals("One Piece", item.name)
        assertEquals(true, item.isNewEpisode)
    }

    private fun seedLegacyDatabaseFile(path: String) {
        val connection = BundledSQLiteDriver().open(path)
        try {
            connection.execSQL("DROP TABLE IF EXISTS anoti_anime_table")
            connection.execSQL("DROP TABLE IF EXISTS room_master_table")
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `anoti_anime_table` (`id` INTEGER NOT NULL, " +
                        "`name` TEXT NOT NULL, `image_url` TEXT, `episodes_aired` INTEGER, " +
                        "`episodes_total` INTEGER, `next_episode_at` TEXT, `aired_on` TEXT, " +
                        "`released_on` TEXT, `score` REAL, `release_status` TEXT NOT NULL, " +
                        "`episodes_viewed` INTEGER NOT NULL, `is_new_episode` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
            )
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS room_master_table " +
                        "(id INTEGER PRIMARY KEY, identity_hash TEXT)"
            )
            connection.execSQL(
                "INSERT OR REPLACE INTO room_master_table (id, identity_hash) " +
                        "VALUES(42, 'e7837677d3018c28ecf262fc6782a530')"
            )
            connection.execSQL("PRAGMA user_version = 1")
            connection.execSQL(
                "INSERT INTO anoti_anime_table (id, name, image_url, episodes_aired, " +
                        "episodes_total, next_episode_at, aired_on, released_on, score, " +
                        "release_status, episodes_viewed, is_new_episode) VALUES " +
                        "(42, 'One Piece', NULL, 10, NULL, NULL, '1999-10-20', NULL, 9.0, " +
                        "'ONGOING', 5, 1)"
            )
        } finally {
            connection.close()
        }
    }
}
