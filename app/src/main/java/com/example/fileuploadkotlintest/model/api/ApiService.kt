package com.ankit.trendinggit.model.api

import com.example.fileuploadkotlintest.model.TestResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @Multipart
    @POST("search/repositories")
    fun uploadImage(@Part imageFile: MultipartBody.Part ):Call<TestResponse>
}