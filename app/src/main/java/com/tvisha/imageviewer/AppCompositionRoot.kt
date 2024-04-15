package com.tvisha.imageviewer

import RetrofitClient
import android.content.Context
import android.content.SharedPreferences
import com.tvisha.imageviewer.database.PhotoDatabase
import com.tvisha.imageviewer.repository.MainRepository
import kotlinx.coroutines.CoroutineScope

val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }

class AppCompositionRoot(private val application: MainApplication) {

    val repository by lazy {
        MainRepository(application)
    }

}