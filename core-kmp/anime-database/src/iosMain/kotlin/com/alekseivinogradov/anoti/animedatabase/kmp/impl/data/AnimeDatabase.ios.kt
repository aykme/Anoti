package com.alekseivinogradov.anoti.animedatabase.kmp.impl.data

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * Public API entry point for a future iOS app to consume — no call site exists yet in this
 * repository (no iOS app has been built here), so the IDE flags it as unused; that's expected
 * for public library surface, not a real defect, hence the explicit suppression.
 */
@Suppress("unused")
fun getAnimeDatabase(): AnimeDatabase {
    return getRoomDatabase(getDatabaseBuilder())
}

@OptIn(ExperimentalForeignApi::class)
private fun getDatabaseBuilder(): RoomDatabase.Builder<AnimeDatabase> {
    val documentDirectory = requireNotNull(
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )?.path
    )
    return Room.databaseBuilder<AnimeDatabase>(
        name = "$documentDirectory/$animeTableName"
    )
}
