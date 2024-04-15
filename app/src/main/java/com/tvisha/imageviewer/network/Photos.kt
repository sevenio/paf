package com.tvisha.imageviewer.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Photos(

    @SerializedName("id")
    val id: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("asset_type")
    val assetType: String,
    @SerializedName("urls")
    val urls: Urls,

    ) : Parcelable

@Parcelize
data class Urls(
    @SerializedName("regular")
    val regular: String,
) : Parcelable