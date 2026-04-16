package com.example.androidapp.data.remote

import com.example.androidapp.data.dto.MahaiaDto
import retrofit2.Call
import retrofit2.http.GET

interface MahaiakApi {
    @GET("api/Mahaiak")
    fun getMahaiak(): Call<List<MahaiaDto>>
}
