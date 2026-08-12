package com.alekseivinogradov.anoti.animedatabase.android.impl.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.ANIME_TABLE_NAME
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.AnimeDatabase
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.getRoomDatabase

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
    val dbFile = appContext.getDatabasePath(ANIME_TABLE_NAME)
    return Room.databaseBuilder<AnimeDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
