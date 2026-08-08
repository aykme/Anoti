package com.alekseivinogradov.anoti.animedatabase.platform.impl.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alekseivinogradov.anoti.animedatabase.platform.api.data.AnimeDao
import com.alekseivinogradov.anoti.animedatabase.platform.api.data.animeTableName
import com.alekseivinogradov.anoti.animedatabase.platform.api.data.model.AnimeDbPlatform

@Database(entities = [AnimeDbPlatform::class], version = 1, exportSchema = false)
abstract class AnimeDatabase : RoomDatabase() {

    abstract fun animeDao(): AnimeDao

    companion object {

        @Volatile
        private var instance: AnimeDatabase? = null

        fun getDatabase(appContext: Context): AnimeDatabase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    appContext.applicationContext,
                    AnimeDatabase::class.java,
                    animeTableName
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                instance = newInstance
                newInstance
            }
        }
    }
}
