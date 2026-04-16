package com.example.androidapp.data.dto

import com.google.gson.annotations.SerializedName

data class ProduktuaDto(
    @SerializedName("id") val id: Int,
    @SerializedName("izena") val izena: String,
    @SerializedName("prezioa") val prezioa: Double,
    @SerializedName("mota") val mota: String,
    @SerializedName("stock") val stock: Int
)
