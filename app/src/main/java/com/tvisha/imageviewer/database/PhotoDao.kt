package com.tvisha.imageviewer.database

import androidx.room.*


@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(entityCallLogs: List<EntityPhoto>)

    @Upsert
    suspend fun updatePhotos(entityPhoto: EntityPhoto)
}
