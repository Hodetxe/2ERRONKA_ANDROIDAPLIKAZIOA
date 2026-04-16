package com.example.androidapp.data.remote

import com.example.androidapp.data.dto.ErreserbaDto
import com.example.androidapp.data.dto.ErreserbaSortuDto
import com.example.androidapp.data.dto.ErreserbaSortuErantzuna
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ErreserbakApi {
    @GET("api/Erreserbak")
    fun getErreserbak(): Call<List<ErreserbaDto>>

    @POST("api/Erreserbak")
    fun createErreserba(@Body erreserba: ErreserbaSortuDto): Call<ErreserbaSortuErantzuna>
}
