package com.example.androidapp.data.dto

data class LoginErantzuna(
    val ok: Boolean,
    val code: String,
    val message: String,
    val data: LangileaDto?
)
