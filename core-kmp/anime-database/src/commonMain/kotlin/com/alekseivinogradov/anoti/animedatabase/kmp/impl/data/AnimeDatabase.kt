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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [AnimeDbEntity::class], version = 2, exportSchema = false)
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
        connection.execSQL(
            "ALTER TABLE $ANIME_TABLE_NAME ADD COLUMN is_extra_info_enabled " +
                "INTEGER NOT NULL DEFAULT 0"
        )
    }
}

internal fun getRoomDatabase(builder: RoomDatabase.Builder<AnimeDatabase>): AnimeDatabase {
    return builder
        .addMigrations(MIGRATION_1_2)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
