package com.tvisha.imageviewer.database

import androidx.paging.PagingSource
import androidx.room.*


@Dao
interface PhotoDao {

    @Upsert
    suspend fun insertPhotos(entityPhotosList: List<EntityPhoto>)

    @Upsert(entity = EntityPhoto::class)
    fun update(obj: EntityPhotoUpdate)

    @Upsert
    suspend fun updatePhotos(entityPhoto: EntityPhoto)

    @Query("select  * from EntityPhoto")
    fun getPhotosPagingList(): PagingSource<Int, EntityPhoto>

    @Query("Delete From EntityPhoto")
    suspend fun clearAllPhotos()

    @Query("SELECT EXISTS(SELECT * FROM EntityPhoto  where :id = EntityPhoto.id and (localPath is not null and localPath != ''))")
    suspend fun isLocalPathExists(id:String): Boolean
}
