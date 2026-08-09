package com.alekseivinogradov.anoti.animedatabase.kmp.impl.data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.model.AnimeDbEntity
import kotlinx.coroutines.Dispatchers

@Database(entities = [AnimeDbEntity::class], version = 1, exportSchema = false)
@ConstructedBy(AnimeDatabaseConstructor::class)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
}

expect object AnimeDatabaseConstructor : RoomDatabaseConstructor<AnimeDatabase> {
    override fun initialize(): AnimeDatabase
}

internal fun getRoomDatabase(builder: RoomDatabase.Builder<AnimeDatabase>): AnimeDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
}
