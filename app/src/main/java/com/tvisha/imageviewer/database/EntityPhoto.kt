package com.tvisha.imageviewer.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EntityPhoto(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val url: String,
    val localPath: String,
    val createdAt: String,
    val updatedAt: String
)