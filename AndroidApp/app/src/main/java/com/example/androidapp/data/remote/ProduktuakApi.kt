package com.example.androidapp.data.remote

import com.example.androidapp.data.dto.ProduktuaDto
import retrofit2.Call
import retrofit2.http.GET

interface ProduktuakApi {
    @GET("api/Produktuak")
    fun getProduktuak(): Call<List<ProduktuaDto>>
}
