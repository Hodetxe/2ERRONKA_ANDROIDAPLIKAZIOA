package com.example.androidapp.presentation.erreserbak

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.androidapp.core.network.ApiClient
import com.example.androidapp.data.dto.ErreserbaDto
import com.example.androidapp.data.dto.MahaiaDto
import com.example.androidapp.data.model.ErreserbakEgoera
import com.example.androidapp.data.remote.ErreserbakApi
import com.example.androidapp.data.remote.MahaiakApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ErreserbakViewModel : ViewModel() {

    var egoera by mutableStateOf(ErreserbakEgoera())
        private set

    private val erreserbakApi: ErreserbakApi by lazy {
        ApiClient.retrofit.create(ErreserbakApi::class.java)
    }

    private val mahaiakApi: MahaiakApi by lazy {
        ApiClient.retrofit.create(MahaiakApi::class.java)
    }

    init {
        lortuDatuak()
    }

    fun lortuDatuak() {
        lortuMahaiak()
        lortuErreserbak()
    }

    fun lortuErreserbak() {
        egoera = egoera.copy(isLoading = true, error = null)

        erreserbakApi.getErreserbak().enqueue(object : Callback<List<ErreserbaDto>> {
            override fun onResponse(call: Call<List<ErreserbaDto>>, response: Response<List<ErreserbaDto>>) {
                if (response.isSuccessful) {
                    val erreserbak = response.body() ?: emptyList()
                    egoera = egoera.copy(
                        erreserbak = erreserbak,
                        isLoading = false
                    )
                } else {
                    egoera = egoera.copy(
                        isLoading = false,
                        error = "Errorea datuak jasotzean: ${response.code()}"
                    )
                }
            }

            override fun onFailure(call: Call<List<ErreserbaDto>>, t: Throwable) {
                egoera = egoera.copy(
                    isLoading = false,
                    error = "Konexio errorea: ${t.message}"
                )
            }
        })
    }

    private fun lortuMahaiak() {
        mahaiakApi.getMahaiak().enqueue(object : Callback<List<MahaiaDto>> {
            override fun onResponse(call: Call<List<MahaiaDto>>, response: Response<List<MahaiaDto>>) {
                if (response.isSuccessful) {
                    val mahaiakList = response.body() ?: emptyList()
                    val mahaiakMap = mahaiakList.associate { it.id to it.zenbakia }
                    egoera = egoera.copy(mahaiak = mahaiakMap)
                }
            }

            override fun onFailure(call: Call<List<MahaiaDto>>, t: Throwable) {
           }
        })
    }
}
