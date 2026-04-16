package com.example.androidapp.data.model

import com.example.androidapp.data.dto.EskariaDto

data class EskariakEgoera(
    val eskariak: List<EskariaDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
