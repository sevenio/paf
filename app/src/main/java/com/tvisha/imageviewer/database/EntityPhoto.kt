package com.tvisha.imageviewer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EntityPhoto(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val url: String,
    @ColumnInfo(defaultValue = "")
    val localPath: String,
    val createdAt: String,
    val updatedAt: String
)

data class EntityPhotoUpdate(
    val id: String,
    val url: String,
    val createdAt: String,
    val updatedAt: String
)