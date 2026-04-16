package com.example.androidapp.data.remote

import com.example.androidapp.data.dto.EskariaDto
import com.example.androidapp.data.dto.EskariaSortuDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface EskariakApi {
    @GET("api/Eskariak")
    fun getEskariak(): Call<List<EskariaDto>>

    @POST("api/Eskariak")
    fun createEskaria(@Body eskaria: EskariaSortuDto): Call<Any>
}
