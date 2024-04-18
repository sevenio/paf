package com.tvisha.imageviewer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.tvisha.imageviewer.MainApplication
import com.tvisha.imageviewer.database.EntityPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewmodel(application: Application) : AndroidViewModel(application) {

    private val repository by lazy {
        (application as MainApplication).appCompositionRoot.repository
    }


    fun getPhotosPagingFlow(): Flow<PagingData<EntityPhoto>> = repository.getPhotosPagingFlow()

    fun downloadPhoto(photo: EntityPhoto) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.photoDownloadTask(photo = photo)
        }
    }


}