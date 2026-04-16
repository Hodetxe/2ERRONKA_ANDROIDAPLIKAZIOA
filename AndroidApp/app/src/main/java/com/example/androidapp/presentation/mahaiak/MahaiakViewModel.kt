package com.example.androidapp.presentation.mahaiak

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.androidapp.core.network.ApiClient
import com.example.androidapp.data.dto.MahaiaDto
import com.example.androidapp.data.remote.MahaiakApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import com.example.androidapp.data.model.TablesState

class MahaiakViewModel : ViewModel() {

    var state by mutableStateOf(TablesState())
        private set

    private val mahaiakApi: MahaiakApi by lazy {
        try {
            ApiClient.retrofit.create(MahaiakApi::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Errorea ApiClient sortzean: ${e.message}", e)
        }
    }

    init {
        loadTables()
    }

    fun loadTables() {
        state = state.copy(isLoading = true, error = null)

        try {
            mahaiakApi.getMahaiak().enqueue(object : Callback<List<MahaiaDto>> {
                override fun onResponse(call: Call<List<MahaiaDto>>, response: Response<List<MahaiaDto>>) {
                    try {
                        if (response.isSuccessful) {
                            state = state.copy(
                                tables = response.body() ?: emptyList(),
                                isLoading = false
                            )
                        } else {
                            state = state.copy(
                                isLoading = false,
                                error = "Errorea datuak jasotzean: ${response.code()}"
                            )
                        }
                    } catch (e: Exception) {
                        state = state.copy(isLoading = false, error = "Datuen prozesamendu errorea")
                    }
                }

                override fun onFailure(call: Call<List<MahaiaDto>>, t: Throwable) {
                    state = state.copy(isLoading = false, error = "Konexio errorea: ${t.message}")
                }
            })
        } catch (e: Exception) {
            state = state.copy(isLoading = false, error = "Errorea eskaera hastean")
        }
    }
}
