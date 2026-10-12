package com.alekseivinogradov.anoti.animedatabase.ios.impl.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.AnimeDatabase
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.getRoomDatabase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AnimeDatabaseContinuityTest {

    @Test
    fun opensAnExistingPreMigrationDatabaseFileWithoutWipingItsData() = runTest {
        //Given
        val dbPath = "anoti_anime_table_continuity_test.db"
        seedLegacyDatabaseFile(dbPath)

        //When
        val database = getRoomDatabase(
            Room.databaseBuilder<AnimeDatabase>(name = dbPath)
        )
        val items = database.animeDao().getAllItems()
        database.close()

        //Then
        assertEquals(1, items.size)
        val item = items.first()
        assertEquals(42, item.id)
        assertEquals("One Piece", item.name)
        assertEquals(true, item.isNewEpisode)
        // Migrated from a pre-existing column-less row: the ADD COLUMN default applies.
        assertEquals(false, item.isExtraInfoEnabled)
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

    @Test
    fun upgradingFromVersionTwoClearsOnlyThePlaceholderImages() = runTest {
        //Given
        val dbPath = "anoti_anime_table_continuity_v2_test.db"
        seedVersionTwoDatabaseFile(dbPath)

        //When
        val database = getRoomDatabase(
            Room.databaseBuilder<AnimeDatabase>(name = dbPath)
        )
        val items = database.animeDao().getAllItems().associateBy { it.id }
        database.close()

        //Then
        assertEquals(VERSION_TWO_ROWS.keys, items.keys)
        assertNull(items.getValue(PLACEHOLDER_ID).imageUrl)
        assertNull(items.getValue(OLD_HOST_PLACEHOLDER_ID).imageUrl)
        assertEquals(REAL_POSTER_URL, items.getValue(REAL_POSTER_ID).imageUrl)
        assertNull(items.getValue(NO_IMAGE_ID).imageUrl)
        // `_` is a LIKE wildcard: only an escaped pattern leaves this look-alike alone.
        assertEquals(LOOK_ALIKE_URL, items.getValue(LOOK_ALIKE_ID).imageUrl)
        assertEquals(true, items.getValue(PLACEHOLDER_ID).isExtraInfoEnabled)
        assertEquals("Placeholder", items.getValue(PLACEHOLDER_ID).name)
    }

    private fun seedVersionTwoDatabaseFile(path: String) {
        val connection = BundledSQLiteDriver().open(path)
        try {
            connection.execSQL("DROP TABLE IF EXISTS anoti_anime_table")
            connection.execSQL("DROP TABLE IF EXISTS room_master_table")
            // Version 2 is version 1 plus the column MIGRATION_1_2 adds, default included: Room
            // checks the table against the entity after migrating.
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS `anoti_anime_table` (`id` INTEGER NOT NULL, " +
                    "`name` TEXT NOT NULL, `image_url` TEXT, `episodes_aired` INTEGER, " +
                    "`episodes_total` INTEGER, `next_episode_at` TEXT, `aired_on` TEXT, " +
                    "`released_on` TEXT, `score` REAL, `release_status` TEXT NOT NULL, " +
                    "`episodes_viewed` INTEGER NOT NULL, `is_new_episode` INTEGER NOT NULL, " +
                    "`is_extra_info_enabled` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`))"
            )
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS room_master_table " +
                    "(id INTEGER PRIMARY KEY, identity_hash TEXT)"
            )
            connection.execSQL(
                "INSERT OR REPLACE INTO room_master_table (id, identity_hash) " +
                    "VALUES(42, '$VERSION_TWO_IDENTITY_HASH')"
            )
            connection.execSQL("PRAGMA user_version = 2")
            VERSION_TWO_ROWS.forEach { (id, imageUrl) ->
                val name = if (id == PLACEHOLDER_ID) "Placeholder" else "Anime $id"
                val isExtraInfoEnabled = if (id == PLACEHOLDER_ID) 1 else 0
                connection.execSQL(
                    "INSERT INTO anoti_anime_table (id, name, image_url, episodes_aired, " +
                        "episodes_total, next_episode_at, aired_on, released_on, score, " +
                        "release_status, episodes_viewed, is_new_episode, " +
                        "is_extra_info_enabled) VALUES ($id, '$name', $imageUrl, 1, NULL, " +
                        "NULL, NULL, NULL, NULL, 'ONGOING', 0, 0, $isExtraInfoEnabled)"
                )
            }
        } finally {
            connection.close()
        }
    }

    private companion object {
        private const val PLACEHOLDER_ID = 1
        private const val OLD_HOST_PLACEHOLDER_ID = 2
        private const val REAL_POSTER_ID = 3
        private const val NO_IMAGE_ID = 4
        private const val LOOK_ALIKE_ID = 5
        private const val VERSION_TWO_IDENTITY_HASH = "0e5d2510514c5becdfd981013772c70b"
        private const val REAL_POSTER_URL = "https://shikimori.io/uploads/poster/animes/3/a.jpeg"
        private const val LOOK_ALIKE_URL =
            "https://shikimori.io/assets/globals/missingXoriginal.jpg"

        // The image_url of each seeded row, as SQL literals.
        private val VERSION_TWO_ROWS = mapOf(
            PLACEHOLDER_ID to "'https://shikimori.io/assets/globals/missing_original.jpg'",
            OLD_HOST_PLACEHOLDER_ID to
                "'https://shikimori.one/assets/globals/missing_original.jpg'",
            REAL_POSTER_ID to "'$REAL_POSTER_URL'",
            NO_IMAGE_ID to "NULL",
            LOOK_ALIKE_ID to "'$LOOK_ALIKE_URL'"
        )
    }
}
