package com.tvisha.imageviewer.repository

import RetrofitClient
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import com.tvisha.imageviewer.MainApplication
import com.tvisha.imageviewer.TAG
import com.tvisha.imageviewer.database.EntityPhoto
import com.tvisha.imageviewer.database.PhotoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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

    private val photoDao by lazy {
        photoDatabase.photoDao
    }

    suspend fun getPhotos(page: Int, pageSize: Int) {
        val response = networkApi.getPhotos(page = page, perPage = pageSize)
        val entityList = response.map {
            EntityPhoto(
                id = it.id,
                url = it.urls.regular,
                localPath = "",
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        }
        photoDao.insertPhotos(entityList)
        MainScope().launch(Dispatchers.IO) {
            entityList.forEach {
                async { photoDownloadTask(application, entityPhoto = it) }
            }
        }
    }

    private suspend fun photoDownloadTask(context: Context, entityPhoto: EntityPhoto){
        val bitmap = downloadPhoto(entityPhoto.url)
        bitmap?.let {
            val localPath = saveBitmap(context = context, bitmap = it, id = entityPhoto.id)
            photoDao.updatePhotos(entityPhoto.copy(localPath = localPath))
        }

    }

    private fun downloadPhoto(imageUrl: String): Bitmap? {
        Log.e(TAG, "$imageUrl")

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

    fun loadImageFromInternalStorage(context: Context, fileName: String, imageView: ImageView) {
        try {
            val fis: FileInputStream = context.openFileInput(fileName)
            val bitmap = BitmapFactory.decodeStream(fis)
            imageView.setImageBitmap(bitmap)
            fis.close()
            Log.d(TAG, "Image loaded from internal storage: $fileName")
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "File not found: " + e.message)
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "Error loading image from internal storage: " + e.message)
        }
    }

}