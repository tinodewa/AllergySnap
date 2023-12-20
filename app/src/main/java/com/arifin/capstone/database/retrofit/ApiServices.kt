package com.arifin.capstone.database.retrofit

import com.arifin.capstone.database.response.ResponsePredict
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiServices {

    @Multipart
    @POST("predict")
    suspend fun postImagePredict(
        @Part image: MultipartBody.Part,
    ): ResponsePredict
}
