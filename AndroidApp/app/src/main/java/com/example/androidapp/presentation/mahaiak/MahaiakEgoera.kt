package com.example.androidapp.presentation.mahaiak

import com.example.androidapp.data.dto.MahaiaDto

data class MahaiakEgoera(
    val mahaiak: List<MahaiaDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
