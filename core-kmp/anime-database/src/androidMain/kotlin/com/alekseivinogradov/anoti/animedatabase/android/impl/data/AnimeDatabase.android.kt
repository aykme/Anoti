package com.alekseivinogradov.anoti.animedatabase.android.impl.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.ANIME_TABLE_NAME
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.AnimeDatabase
import com.alekseivinogradov.anoti.animedatabase.kmp.impl.data.getRoomDatabase

/**
 * Opens the app's database. The app-wide DI binding is what keeps the single instance, so each
 * call here opens a new one.
 */
fun getAnimeDatabase(context: Context): AnimeDatabase =
    getRoomDatabase(getDatabaseBuilder(context))

internal fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AnimeDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(ANIME_TABLE_NAME)
    return Room.databaseBuilder<AnimeDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
