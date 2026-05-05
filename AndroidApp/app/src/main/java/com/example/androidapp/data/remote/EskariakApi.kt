package com.example.androidapp.data.remote

import com.example.androidapp.data.dto.EskariaDto
import com.example.androidapp.data.dto.EskariaSortuDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface EskariakApi {
    @GET("api/Eskariak")
    fun getEskariak(): Call<List<EskariaDto>>

    @GET("api/Eskariak/{id}")
    fun getEskaria(@Path("id") id: Int): Call<EskariaDto>

    @POST("api/Eskariak")
    fun createEskaria(@Body eskaria: EskariaSortuDto): Call<Any>

    @PUT("api/Eskariak/{id}")
    fun updateEskaria(
        @Path("id") id: Int,
        @Body eskaria: EskariaSortuDto
    ): Call<Any>
}
