package com.alekseivinogradov.anoti.animedatabase.ios.impl.data

import androidx.room.Room
import androidx.room.RoomDatabase
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.ANIME_TABLE_NAME
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.AnimeDatabase
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.getRoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * Builds the iOS app's database, kept in the app's documents directory. The iOS
 * `DiAnimeDatabasePlatformComponent` builds it once per process.
 */
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
        name = "$documentDirectory/$ANIME_TABLE_NAME"
    )
}
