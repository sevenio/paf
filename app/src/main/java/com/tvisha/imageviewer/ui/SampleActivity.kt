package com.tvisha.imageviewer.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.tvisha.imageviewer.databinding.ActivitySampleBinding

class SampleActivity : AppCompatActivity(){

    private val binding by lazy {
        ActivitySampleBinding.inflate(layoutInflater)
    }

    private val viewModel: MainViewmodel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btnPress.setOnClickListener {
            viewModel.getPhotos()
        }
    }
}