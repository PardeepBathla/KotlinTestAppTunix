package com.ankit.trendinggit.model.api

import com.ankit.trendinggit.model.TestResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("search/repositories")
    suspend fun uploadImage(): Call<TestResponse>
}