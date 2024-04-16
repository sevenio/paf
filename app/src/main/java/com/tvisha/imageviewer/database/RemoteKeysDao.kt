package com.tvisha.imageviewer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface RemoteKeysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<RemoteKeys>)

    @Query("Select * From remote_key Where id LIKE :id")
    suspend fun getRemoteKeyByPhotoID(id: String): RemoteKeys?

    @Query("Delete From remote_key")
    suspend fun clearRemoteKeys()

    @Query("Select createdAt From remote_key Order By createdAt DESC LIMIT 1")
    suspend fun getCreationTime(): String?

}