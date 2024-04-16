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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
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
                photoDatabase.photoDao.getPhotosPagingList( )
            },
            remoteMediator = PhotosRemoteMediator(
                context = application,
                networkApi = networkApi,
                photoDatabase = photoDatabase,
            )
        ).flow





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