package com.tvisha.imageviewer.repository

import RetrofitClient
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.ContactsContract.Contacts.Photo
import android.util.Log
import android.widget.ImageView
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.tvisha.imageviewer.MainApplication
import com.tvisha.imageviewer.TAG
import com.tvisha.imageviewer.database.EntityPhoto
import com.tvisha.imageviewer.database.PhotoDatabase
import com.tvisha.imageviewer.network.Photos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class MainRepository(private val application: MainApplication) {

    private val retrofitClient by lazy {
        RetrofitClient()
    }

    private val photoDatabase by lazy {
        PhotoDatabase.DatabaseManager(application).createDatabase()
    }

    private val networkApi by lazy {
        retrofitClient.networkService
    }


    companion object {
        const val PAGE_SIZE = 20
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getPhotosPagingFlow(): Flow<PagingData<EntityPhoto>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = 10,
                initialLoadSize = PAGE_SIZE,
            ),
            pagingSourceFactory = {
                photoDatabase.photoDao.getPhotosPagingList()
            },
            remoteMediator = PhotosRemoteMediator(
                context = application,
                networkApi = networkApi,
                photoDatabase = photoDatabase,
            )
        ).flow


    private suspend fun photoDownloadTask(context: Context, photo: Photos) {


        if (!photoDatabase.photoDao.isLocalPathExists(photo.id)) {

            val bitmap = downloadPhoto(photo.urls.regular)
            bitmap?.let {
                val localPath =
                    saveBitmap(context = context, bitmap = it, id = photo.id)
                photoDatabase.photoDao.updatePhotos(
                    EntityPhoto(
                        id = photo.id,
                        url = photo.urls.regular,
                        createdAt = photo.createdAt,
                        updatedAt = photo.updatedAt,
                        localPath = localPath
                    )
                )
            }
        }
    }


    private fun downloadPhoto(imageUrl: String): Bitmap? {
        Log.d("downloadPhoto", "download $imageUrl")

        return try {
            val url = URL(imageUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.e("downloadPhoto", "Error downloading image: " + "$e")
            null
        }
    }

    private fun saveBitmap(context: Context, bitmap: Bitmap, id: String): String {
        Log.d("downloadPhoto", "save $id")

        return try {
            val outputStream: FileOutputStream =
                context.openFileOutput("image_${id}.png", Context.MODE_PRIVATE)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
            return "image_${id}.png"
        } catch (e: java.lang.Exception) {
            Log.e("downloadPhoto", "Error saving bitmap to internal storage: " + e.message)
            ""
        }
    }

}