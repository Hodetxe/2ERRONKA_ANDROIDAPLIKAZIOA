package com.example.androidapp.presentation.erreserbak

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.androidapp.core.network.ApiClient
import com.example.androidapp.data.dto.ErreserbaSortuDto
import com.example.androidapp.data.dto.ErreserbaSortuErantzuna
import com.example.androidapp.data.dto.MahaiaDto
import com.example.androidapp.data.remote.ErreserbakApi
import com.example.androidapp.data.remote.MahaiakApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ErreserbaSortuViewModel : ViewModel() {

    var state by mutableStateOf(CreateReservationState())
        private set

    private val erreserbakApi: ErreserbakApi by lazy {
        ApiClient.retrofit.create(ErreserbakApi::class.java)
    }

    private val mahaiakApi: MahaiakApi by lazy {
        ApiClient.retrofit.create(MahaiakApi::class.java)
    }

    init {
        loadTables()
    }

    private fun loadTables() {
        state = state.copy(isLoading = true)
        mahaiakApi.getMahaiak().enqueue(object : Callback<List<MahaiaDto>> {
            override fun onResponse(call: Call<List<MahaiaDto>>, response: Response<List<MahaiaDto>>) {
                if (response.isSuccessful) {
                    state = state.copy(
                        tables = response.body() ?: emptyList(),
                        isLoading = false
                    )
                } else {
                    state = state.copy(isLoading = false, error = "Errorea mahaiak kargatzean")
                }
            }

            override fun onFailure(call: Call<List<MahaiaDto>>, t: Throwable) {
                state = state.copy(isLoading = false, error = "Konexio errorea: ${t.message}")
            }
        })
    }

    fun onCustomerNameChanged(value: String) {
        state = state.copy(customerName = value)
    }

    fun onPhoneChanged(value: String) {
        state = state.copy(phone = value)
    }

    fun onPersonCountChanged(value: String) {
        // Only allow numeric input
        if (value.all { it.isDigit() }) {
            state = state.copy(personCount = value)
        }
    }

    fun onDateChanged(value: String) {
        state = state.copy(date = value)
    }

    fun onTimeChanged(value: String) {
        state = state.copy(time = value)
    }

    fun onTableSelected(table: MahaiaDto) {
        state = state.copy(selectedTable = table)
    }

    fun createReservation() {
        val personCount = state.personCount.toIntOrNull()
        val table = state.selectedTable

        if (state.customerName.isBlank() || state.phone.isBlank() || 
            personCount == null || state.date.isBlank() || state.time.isBlank() || table == null) {
            state = state.copy(error = "Eremu guztiak bete behar dira")
            return
        }

        // Combine date and time to ISO format: YYYY-MM-DDTHH:mm:00
        val dateTime = "${state.date}T${state.time}:00"

        val reservation = ErreserbaSortuDto(
            bezeroIzena = state.customerName,
            telefonoa = state.phone,
            pertsonaKopurua = personCount,
            egunaOrdua = dateTime,
            prezioTotala = 0.0, // Initial price is 0
            langileaId = 1, // TODO: Get logged in user ID. Hardcoded for now as I don't have auth context storage yet
            mahaiakId = table.id
        )

        state = state.copy(isLoading = true, error = null)

        erreserbakApi.createErreserba(reservation).enqueue(object : Callback<ErreserbaSortuErantzuna> {
            override fun onResponse(call: Call<ErreserbaSortuErantzuna>, response: Response<ErreserbaSortuErantzuna>) {
                if (response.isSuccessful) {
                    state = state.copy(isLoading = false, isSuccess = true)
                } else {
                    state = state.copy(isLoading = false, error = "Errorea sortzean: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ErreserbaSortuErantzuna>, t: Throwable) {
                state = state.copy(isLoading = false, error = "Konexio errorea: ${t.message}")
            }
        })
    }
    
    fun resetSuccess() {
        state = state.copy(isSuccess = false)
    }
}
