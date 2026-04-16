package com.example.androidapp.data.model

import com.example.androidapp.data.dto.MahaiaDto

data class TablesState(
    val tables: List<MahaiaDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
