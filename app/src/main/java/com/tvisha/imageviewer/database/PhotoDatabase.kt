package com.tvisha.imageviewer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tvisha.imageviewer.BuildConfig


@Database(
    entities = [
        EntityPhoto::class,
        RemoteKeys::class
    ], version = 9, exportSchema = true
)

abstract class PhotoDatabase : RoomDatabase() {

    abstract val photoDao: PhotoDao
    abstract val remoteKeysDao: RemoteKeysDao

    companion object {
        private val DATABASE_NAME = "com_tvisha_photo"
    }

    class DatabaseManager(val context: Context) {


        fun createDatabase() = if (BuildConfig.DEBUG) {
            Room.databaseBuilder(context, PhotoDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
        } else {
            Room.databaseBuilder(context, PhotoDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }

    }
}