package com.example.androidapp.presentation.eskariak

import com.example.androidapp.data.dto.ErreserbaDto
import com.example.androidapp.data.dto.ProduktuaDto

data class EskariaSortuEgoera(
    val produktuak: List<ProduktuaDto> = emptyList(),
    val kategoriak: List<String> = emptyList(),
    val kategoriaAukeratua: String = "",
    val saskia: Map<Int, Int> = emptyMap(), // ProductId -> Quantity
    val erreserbak: List<ErreserbaDto> = emptyList(), // Available reservations
    val erreserbaId: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
