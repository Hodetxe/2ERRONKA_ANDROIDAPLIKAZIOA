package com.example.androidapp.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.androidapp.core.network.ApiClient
import com.example.androidapp.data.dto.LangileaDto
import com.example.androidapp.data.dto.LoginEskaera
import com.example.androidapp.data.dto.LoginErantzuna
import com.example.androidapp.data.model.LoginField
import com.example.androidapp.data.model.LoginState
import com.example.androidapp.data.remote.LoginApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginPantailaViewModel : ViewModel() {

    var state by mutableStateOf(LoginState())
        private set

    private val loginApi: LoginApi by lazy {
        ApiClient.retrofit.create(LoginApi::class.java)
    }

    fun onFieldSelected(field: LoginField) {
        state = state.copy(focusedField = field)
    }

    fun onKeyPress(key: String) {
        when (key) {
            "←" -> {
                state = state.copy(
                    workerCode = if (state.focusedField == LoginField.Code)
                        state.workerCode.dropLast(1) else state.workerCode,
                    password = if (state.focusedField == LoginField.Password)
                        state.password.dropLast(1) else state.password
                )
            }
            else -> {
                if (key.isNotEmpty()) {
                    state = if (state.focusedField == LoginField.Code) {
                        state.copy(workerCode = state.workerCode + key)
                    } else {
                        state.copy(password = state.password + key)
                    }
                }
            }
        }
    }

    fun login(
        onSuccess: (LangileaDto) -> Unit,
        onError: (String) -> Unit
    ) {
        val code = state.workerCode.toIntOrNull()
        if (code == null || state.password.isBlank()) {
            onError("Kodea zenbaki bat izan behar da eta pasahitza ezin da hutsik egon.")
            return
        }

        state = state.copy(isLoading = true)

        val request = LoginEskaera(
            Langile_kodea = code,
            Pasahitza = state.password
        )

        loginApi.login(request).enqueue(object : Callback<LoginErantzuna> {
            override fun onResponse(call: Call<LoginErantzuna>, response: Response<LoginErantzuna>) {
                state = state.copy(isLoading = false)

                if (!response.isSuccessful) {
                    onError("Errorea: ${response.code()}")
                    return
                }

                val erantzuna = response.body()
                if (erantzuna == null) {
                    onError("Errorea: erantzun hutsa")
                    return
                }

                if (!erantzuna.ok) {
                    onError(erantzuna.message)
                    return
                }

                val langilea = erantzuna.data
                if (langilea == null) {
                    onError("Errorea: daturik ez")
                    return
                }

                onSuccess(langilea)
            }

            override fun onFailure(call: Call<LoginErantzuna>, t: Throwable) {
                state = state.copy(isLoading = false)
                onError("Sare errorea: ${t.message ?: "Ezezaguna"}")
            }
        })
    }
}
