package com.example.androidapp.presentation.erreserbak

import com.example.androidapp.data.dto.MahaiaDto

data class ErreserbaSortuEgoera(
    val bezeroIzena: String = "",
    val telefonoa: String = "",
    val pertsonaKopurua: String = "",
    val data: String = "",
    val ordua: String = "",
    val aukeratutakoMahaia: MahaiaDto? = null,
    val mahaiak: List<MahaiaDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
