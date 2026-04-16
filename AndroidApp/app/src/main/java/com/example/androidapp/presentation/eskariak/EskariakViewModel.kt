package com.example.androidapp.presentation.eskariak

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapp.core.network.ApiClient
import com.example.androidapp.data.dto.EskariaDto
import com.example.androidapp.data.model.OrdersState
import com.example.androidapp.data.remote.EskariakApi
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EskariakViewModel : ViewModel() {

    var state by mutableStateOf(OrdersState())
        private set

    private val eskariakApi: EskariakApi by lazy {
        try {
            ApiClient.retrofit.create(EskariakApi::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Errorea ApiClient sortzean: ${e.message}", e)
        }
    }

    private val erreserbakApi: com.example.androidapp.data.remote.ErreserbakApi by lazy {
        try {
            ApiClient.retrofit.create(com.example.androidapp.data.remote.ErreserbakApi::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Errorea ApiClient sortzean: ${e.message}", e)
        }
    }

    init {
        android.util.Log.d("EskariakViewModel", "ViewModel init called")
        fetchOrders()
    }

    fun fetchOrders() {
        android.util.Log.d("EskariakViewModel", "fetchOrders called")
        state = state.copy(isLoading = true, error = null)

        try {
            eskariakApi.getEskariak().enqueue(object : Callback<List<EskariaDto>> {
                override fun onResponse(call: Call<List<EskariaDto>>, response: Response<List<EskariaDto>>) {
                    try {
                        if (response.isSuccessful) {
                            val orders = response.body() ?: emptyList()
                            
                            // If API didn't return customer names (e.g. old API version), try to fetch them from reservations
                            val needsEnrichment = orders.any { it.bezeroIzena == null || it.bezeroIzena == "Ezezaguna" || it.bezeroIzena == "Bezero ezezaguna" }
                            
                            if (needsEnrichment) {
                                fetchReservationsAndEnrich(orders)
                            } else {
                                state = state.copy(orders = orders, isLoading = false)
                            }
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Unknown error"
                            state = state.copy(
                                isLoading = false,
                                error = "Errorea datuak jasotzean: ${response.code()} - $errorBody"
                            )
                        }
                    } catch (e: Exception) {
                        state = state.copy(isLoading = false, error = "Datuen prozesamendu errorea: ${e.message}")
                    }
                }

                override fun onFailure(call: Call<List<EskariaDto>>, t: Throwable) {
                    state = state.copy(isLoading = false, error = "Konexio errorea: ${t.message}")
                }
            })
        } catch (e: Exception) {
            state = state.copy(isLoading = false, error = "Errorea eskaera hastean: ${e.message}")
        }
    }

    private fun fetchReservationsAndEnrich(eskariak: List<EskariaDto>) {
        erreserbakApi.getErreserbak().enqueue(object : Callback<List<com.example.androidapp.data.dto.ErreserbaDto>> {
            override fun onResponse(call: Call<List<com.example.androidapp.data.dto.ErreserbaDto>>, response: Response<List<com.example.androidapp.data.dto.ErreserbaDto>>) {
                if (response.isSuccessful) {
                    val erreserbak = response.body() ?: emptyList()
                    val erreserbaMap = erreserbak.associateBy { it.id }
                    
                    val enrichedOrders = eskariak.map { eskaria ->
                        if (eskaria.bezeroIzena == null || eskaria.bezeroIzena == "Ezezaguna" || eskaria.bezeroIzena == "Bezero ezezaguna") {
                            val erreserba = erreserbaMap[eskaria.erreserbaId]
                            if (erreserba != null) {
                                eskaria.copy(bezeroIzena = erreserba.bezeroIzena)
                            } else {
                                eskaria
                            }
                        } else {
                            eskaria
                        }
                    }
                    state = state.copy(orders = enrichedOrders, isLoading = false)
                } else {
                    // Fallback to original list if reservations fetch fails
                    state = state.copy(orders = eskariak, isLoading = false)
                }
            }

            override fun onFailure(call: Call<List<com.example.androidapp.data.dto.ErreserbaDto>>, t: Throwable) {
                // Fallback to original list if reservations fetch fails
                state = state.copy(orders = eskariak, isLoading = false)
            }
        })
    }
}
