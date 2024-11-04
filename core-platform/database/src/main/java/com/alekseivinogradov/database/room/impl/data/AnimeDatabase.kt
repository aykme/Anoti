package com.alekseivinogradov.database.room.impl.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alekseivinogradov.database.room.api.data.AnimeDao
import com.alekseivinogradov.database.room.api.data.animeTableName
import com.alekseivinogradov.database.room.api.data.model.AnimeDbPlatform

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
                    .fallbackToDestructiveMigration()
                    .build()
                instance = newInstance
                newInstance
            }
        }
    }
}
