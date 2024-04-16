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
import com.tvisha.imageviewer.TAG
import com.tvisha.imageviewer.database.EntityPhoto
import com.tvisha.imageviewer.database.EntityPhotoUpdate
import com.tvisha.imageviewer.database.PhotoDatabase
import com.tvisha.imageviewer.database.RemoteKeys
import com.tvisha.imageviewer.network.NetworkApi
import com.tvisha.imageviewer.network.Photos
import com.tvisha.imageviewer.network.Urls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
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

    companion object{
        const val PER_PAGE = 20
    }

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
            Log.d(TAG, "$page")
//            val apiResponse = getPhotosArrayList(page = (page - 1) * PER_PAGE + 1)
            val apiResponse = networkApi.getPhotos(page = page)
            val endOfPaginationReached = apiResponse.isEmpty()

            photoDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    photoDatabase.remoteKeysDao.clearRemoteKeys()
                    photoDatabase.photoDao.clearAllPhotos()
                }
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
                apiResponse.map {
                    val updatedObject = EntityPhotoUpdate(
                        id = it.id,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        url = it.urls.regular
                    )
                    photoDatabase.photoDao.update(updatedObject)
                    updatedObject
                }
            }

            MainScope().launch(Dispatchers.IO) {
                photoDownloadTask(
                    context, apiResponse
                )
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

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, EntityPhoto>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                photoDatabase.remoteKeysDao.getRemoteKeyByPhotoID(id)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, EntityPhoto>): RemoteKeys? {
        return state.pages.firstOrNull {
            it.data.isNotEmpty()
        }?.data?.firstOrNull()?.let { photo ->
            photoDatabase.remoteKeysDao.getRemoteKeyByPhotoID(photo.id)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, EntityPhoto>): RemoteKeys? {
        return state.pages.lastOrNull {
            it.data.isNotEmpty()
        }?.data?.lastOrNull()?.let { photo ->
            photoDatabase.remoteKeysDao.getRemoteKeyByPhotoID(photo.id)
        }
    }

    fun getPhotosArrayList(page: Int): ArrayList<Photos>{
//        https://images.unsplash.com/photo-1713145868370-0b9c9bb58465?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w1OTA3ODV8MHwxfGFsbHw1MXx8fHx8fDJ8fDE3MTMyNjE2MjJ8&ixlib=rb-4.0.3&q=80&w=1080
        val arrayList = arrayListOf<Photos>()
        for (i in page  .. page + (PER_PAGE - 1)){
            arrayList.add(Photos(id = i.toString(), createdAt = "", updatedAt = "", urls = Urls(regular = "https://images.unsplash.com/photo-1713145868370-0b9c9bb58465?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w1OTA3ODV8MHwxfGFsbHw1MXx8fHx8fDJ8fDE3MTMyNjE2MjJ8&ixlib=rb-4.0.3&q=80&w=1080"), assetType = "photo" ))
        }
        return arrayList

    }
//    suspend fun getPhotos(page: Int, pageSize: Int):ArrayList<Photos> {
//        val response = networkApi.getPhotos(page = page, perPage = pageSize)
//        val entityList = response.map {
//            EntityPhoto(
//                id = it.id,
//                url = it.urls.regular,
//                localPath = "",
//                createdAt = it.createdAt,
//                updatedAt = it.updatedAt
//            )
//        }
//        photoDatabase.photoDao.insertPhotos(entityList)
//        MainScope().launch(Dispatchers.IO) {
//            entityList.forEach {
//                async { photoDownloadTask(context, entityPhoto = it) }
//            }
//        }
//        return response
//    }
    private suspend fun photoDownloadTask(context: Context, photosList: ArrayList<Photos>){

            photosList.forEach { photo ->
//                if(!photoDatabase.photoDao.isLocalPathExists(photo.id)) {
                    val bitmap = downloadPhoto(photo.urls.regular)
                    bitmap?.let {
                        val localPath = saveBitmap(context = context, bitmap = it, id = photo.id)
                        photoDatabase.photoDao.updatePhotos(EntityPhoto(id = photo.id, url = photo.urls.regular, createdAt = photo.createdAt, updatedAt = photo.updatedAt, localPath = localPath))
                    }
//                }
            }

    }

    private fun downloadPhoto(imageUrl: String): Bitmap? {
        Log.d(TAG, "download $imageUrl")

        return try {
            val url = URL(imageUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading image: " + "$e")
            null
        }
    }

    private fun saveBitmap(context: Context, bitmap: Bitmap, id: String): String {
        Log.d(TAG, "save $id")

        return try {
            val outputStream: FileOutputStream = context.openFileOutput("image_${id}.png", Context.MODE_PRIVATE)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
            return "image_${id}.png"
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "Error saving bitmap to internal storage: " + e.message)
            ""
        }
    }
}