package com.alekseivinogradov.anoti.animedatabase.kmp.impl.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

private object AnimeDatabaseHolder {
    @Volatile
    var instance: AnimeDatabase? = null
}

fun getAnimeDatabase(context: Context): AnimeDatabase {
    return AnimeDatabaseHolder.instance ?: synchronized(AnimeDatabaseHolder) {
        AnimeDatabaseHolder.instance ?: getRoomDatabase(getDatabaseBuilder(context)).also {
            AnimeDatabaseHolder.instance = it
        }
    }
}

internal fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AnimeDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(animeTableName)
    return Room.databaseBuilder<AnimeDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
