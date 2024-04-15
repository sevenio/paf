package com.tvisha.imageviewer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tvisha.imageviewer.MainApplication
import kotlinx.coroutines.launch

class MainViewmodel(application: Application) : AndroidViewModel(application) {

    private val repository by lazy {
        (application as MainApplication).appCompositionRoot.repository
    }

    fun getPhotos() {
        viewModelScope.launch {
            repository.getPhotos(page = 1, pageSize = 10)

        }
    }


}