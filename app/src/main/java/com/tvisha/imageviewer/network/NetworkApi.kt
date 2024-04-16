package com.tvisha.imageviewer.network

import retrofit2.http.*

interface NetworkApi {

    @GET("photos")
    suspend fun getPhotos(
        @Query("client_id")
        clientId: String = "rAnMq6xp5vmoDkAhC-NU3tog8YqDgN9bxEoxw1dG89k",
        @Query("page")
        page: Int = 1,
        @Query("per_page")
        perPage: Int = 20,
        @Query("order_by")
        orderBy: String = "latest",
    ): ArrayList<Photos>

}