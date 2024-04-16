package com.tvisha.imageviewer.database

import androidx.paging.PagingSource
import androidx.room.*


@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(entityCallLogs: List<EntityPhoto>)

    @Upsert(entity = EntityPhoto::class)
    fun update(obj: EntityPhotoUpdate)

    @Upsert
    suspend fun updatePhotos(entityPhoto: EntityPhoto)


    @Query("select  * from EntityPhoto order by updatedAt DESC")
    fun getPhotosPagingList(): PagingSource<Int, EntityPhoto>

    @Query("Delete From EntityPhoto")
    suspend fun clearAllPhotos()

    @Query("SELECT EXISTS(SELECT * FROM EntityPhoto  where :id Like EntityPhoto.id and (localPath is null or localPath = ''))")
    suspend fun isLocalPathExists(id:String): Boolean
}
