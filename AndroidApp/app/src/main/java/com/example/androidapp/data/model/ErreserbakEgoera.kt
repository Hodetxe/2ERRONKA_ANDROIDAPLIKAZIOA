package com.example.androidapp.data.model

import com.example.androidapp.data.dto.ErreserbaDto

data class ErreserbakEgoera(
    val erreserbak: List<ErreserbaDto> = emptyList(),
    val mahaiak: Map<Int, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)
