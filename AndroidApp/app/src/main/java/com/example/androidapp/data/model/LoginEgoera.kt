package com.example.androidapp.data.model

data class LoginEgoera(
    val langileKodea: String = "",
    val pasahitza: String = "",
    val eremuEzarri : EremuEzarri = EremuEzarri.Kodea,
    val loading: Boolean = false,
    val erroreMezua: String? = null
)
