package com.example.androidapp.presentation.erreserbak

import com.example.androidapp.data.dto.MahaiaDto

data class CreateReservationState(
    val customerName: String = "",
    val phone: String = "",
    val personCount: String = "",
    val date: String = "",
    val time: String = "",
    val selectedTable: MahaiaDto? = null,
    val tables: List<MahaiaDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
