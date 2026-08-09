package com.alekseivinogradov.anoti.animedatabase.kmp.impl.data

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

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
