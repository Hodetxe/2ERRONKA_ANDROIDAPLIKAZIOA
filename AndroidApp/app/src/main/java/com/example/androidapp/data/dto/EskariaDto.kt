package com.example.androidapp.data.dto

import com.google.gson.annotations.SerializedName

data class EskariaDto(
    @SerializedName("id") val id: Int,
    @SerializedName("prezioa") val prezioa: Double,
    @SerializedName("egoera") val egoera: String?,
    @SerializedName("erreserbaId") val erreserbaId: Int,
    @SerializedName("bezeroIzena") val bezeroIzena: String?,
    @SerializedName("mahaiaZenbakia") val mahaiaZenbakia: Int?,
    @SerializedName("produktuak") val produktuak: List<EskariaProduktuaDto>?
)

data class EskariaProduktuaDto(
    @SerializedName("produktuaId") val produktuaId: Int,
    @SerializedName("produktuaIzena") val produktuaIzena: String?,
    @SerializedName("kantitatea") val kantitatea: Int,
    @SerializedName("prezioa") val prezioa: Double,
    @SerializedName("prezioaGuztira") val prezioaGuztira: Double
)
