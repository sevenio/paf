package com.tvisha.imageviewer.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.google.gson.Gson
import com.tvisha.imageviewer.TAG
import com.tvisha.imageviewer.database.EntityPhoto
import com.tvisha.imageviewer.database.EntityPhotoUpdate
import com.tvisha.imageviewer.database.PhotoDatabase
import com.tvisha.imageviewer.database.RemoteKeys
import com.tvisha.imageviewer.network.NetworkApi
import com.tvisha.imageviewer.network.Photos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import retrofit2.HttpException
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


@OptIn(ExperimentalPagingApi::class)
class PhotosRemoteMediator(
    private val context: Context,
    private val networkApi: NetworkApi,
    private val photoDatabase: PhotoDatabase,
) : RemoteMediator<Int, EntityPhoto>() {



    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EntityPhoto>
    ): MediatorResult {
        val page: Int = when (loadType) {

            LoadType.REFRESH -> {
                1
            }

            LoadType.PREPEND -> {
                 return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
            }
        }

        try {
            Log.d(TAG, "$page ${loadType}")
            val apiResponse = networkApi.getPhotos(page = page)
            val endOfPaginationReached = apiResponse.isEmpty()

            photoDatabase.withTransaction {
//                if (loadType == LoadType.REFRESH) {
//                    photoDatabase.remoteKeysDao.clearRemoteKeys()
//                    photoDatabase.photoDao.clearAllPhotos()
//                }
                val prevKey = if (page > 1) page - 1 else null
                val nextKey = if (endOfPaginationReached) null else page + 1
                val remoteKeys = apiResponse.map {
                    RemoteKeys(
                        id = it.id,
                        prevKey = prevKey,
                        currentPage = page,
                        nextKey = nextKey,
                        createdAt = it.createdAt
                    )
                }

                photoDatabase.remoteKeysDao.insertAll(remoteKeys)
                val entityPhotoList = apiResponse.map {
                   EntityPhoto(
                        id = it.id,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        url = it.urls.regular,
                        localPath = ""
                    )
                }
                photoDatabase.photoDao.insertPhotos(entityPhotosList = entityPhotoList)

            }


            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)

        } catch (error: IOException) {
            Log.d(TAG, error.message?:"IOException")
            return MediatorResult.Error(error)
        } catch (error: HttpException) {
            Log.d(TAG, error.message?:"HttpException")

            return MediatorResult.Error(error)
        }
    }


    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, EntityPhoto>): RemoteKeys? {
        return state.pages.lastOrNull {
            it.data.isNotEmpty()
        }?.data?.lastOrNull()?.let { photo ->
            photoDatabase.remoteKeysDao.getRemoteKeyByPhotoID(photo.id)
        }
    }

}