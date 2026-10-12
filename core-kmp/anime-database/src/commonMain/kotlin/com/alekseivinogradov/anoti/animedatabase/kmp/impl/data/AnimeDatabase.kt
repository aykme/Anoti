package com.alekseivinogradov.anoti.animedatabase.kmp.impl.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.model.AnimeDbEntity
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.ANOTI_TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [AnimeDbEntity::class], version = 3, exportSchema = false)
@ConstructedBy(AnimeDatabaseConstructor::class)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
}

expect object AnimeDatabaseConstructor : RoomDatabaseConstructor<AnimeDatabase> {
    override fun initialize(): AnimeDatabase
}

/** Adds the extra-info display mode column; existing rows default to it being off. */
internal val MIGRATION_1_2 = object : Migration(startVersion = 1, endVersion = 2) {
    override fun migrate(connection: SQLiteConnection) {
        println("$ANOTI_TAG AnimeDatabase: migrates from 1 to 2")
        connection.execSQL(
            "ALTER TABLE $ANIME_TABLE_NAME ADD COLUMN is_extra_info_enabled " +
                "INTEGER NOT NULL DEFAULT 0"
        )
    }
}

/**
 * Clears the placeholder image URLs REST handed out, under either host the app has used, so the
 * next update writes the real posters. `_` is a LIKE wildcard, hence the escape.
 */
internal val MIGRATION_2_3 = object : Migration(startVersion = 2, endVersion = 3) {
    override fun migrate(connection: SQLiteConnection) {
        println("$ANOTI_TAG AnimeDatabase: migrates from 2 to 3")
        connection.execSQL(
            "UPDATE $ANIME_TABLE_NAME SET image_url = NULL " +
                "WHERE image_url LIKE '%/assets/globals/missing\\_%' ESCAPE '\\'"
        )
    }
}

/** Logs when the database file is created and each time it is opened. */
private object OpeningLog : RoomDatabase.Callback() {
    override fun onCreate(connection: SQLiteConnection) {
        println("$ANOTI_TAG AnimeDatabase: created")
    }

    override fun onOpen(connection: SQLiteConnection) {
        println("$ANOTI_TAG AnimeDatabase: opened")
    }
}

internal fun getRoomDatabase(builder: RoomDatabase.Builder<AnimeDatabase>): AnimeDatabase {
    return builder
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .addCallback(OpeningLog)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
