package com.example.androidapp.data.dto

import com.google.gson.annotations.SerializedName

data class EskariaSortuDto(
    @SerializedName("erreserbaId") val erreserbaId: Int,
    @SerializedName("prezioa") val prezioa: Double,
    @SerializedName("egoera") val egoera: String,
    @SerializedName("produktuak") val produktuak: List<EskariaProduktuaSortuDto>
)

data class EskariaProduktuaSortuDto(
    @SerializedName("produktuaId") val produktuaId: Int,
    @SerializedName("kantitatea") val kantitatea: Int,
    @SerializedName("prezioa") val prezioa: Double
)
