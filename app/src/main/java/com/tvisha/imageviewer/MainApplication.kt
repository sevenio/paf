package com.tvisha.imageviewer

import android.app.Application
import android.util.Log

class MainApplication: Application() {
    val appCompositionRoot by lazy {
        AppCompositionRoot(this)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("ganga", "applicaton on create")
    }
}