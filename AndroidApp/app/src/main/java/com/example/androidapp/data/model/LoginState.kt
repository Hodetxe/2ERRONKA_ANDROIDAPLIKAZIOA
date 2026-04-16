package com.example.androidapp.data.model

data class LoginState(
    val workerCode: String = "",
    val password: String = "",
    val focusedField: LoginField = LoginField.Code,
    val isLoading: Boolean = false,
    val error: String? = null
)
