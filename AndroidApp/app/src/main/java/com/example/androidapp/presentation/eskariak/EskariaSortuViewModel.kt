package com.example.androidapp.presentation.eskariak

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.androidapp.core.network.ApiClient
import com.example.androidapp.data.dto.ProduktuaDto
import com.example.androidapp.data.dto.ErreserbaDto
import com.example.androidapp.data.dto.EskariaDto
import com.example.androidapp.data.dto.EskariaSortuDto
import com.example.androidapp.data.dto.EskariaProduktuaSortuDto
import com.example.androidapp.data.remote.ProduktuakApi
import com.example.androidapp.data.remote.ErreserbakApi
import com.example.androidapp.data.remote.EskariakApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class EskariaSortuViewModel : ViewModel() {

    // Helper property to map 'prezioa' to 'salneurria' if needed, or just use 'prezioa' directly.
    // However, looking at usage, it seems we used .salneurria in the code.
    // Let's fix the usage instead of creating an extension property without accessors.
    
    var egoera by mutableStateOf(EskariaSortuEgoera())
        private set

    private val produktuakApi: ProduktuakApi by lazy {
        ApiClient.retrofit.create(ProduktuakApi::class.java)
    }

    private val erreserbakApi: ErreserbakApi by lazy {
        ApiClient.retrofit.create(ErreserbakApi::class.java)
    }

    private val eskariakApi: EskariakApi by lazy {
        ApiClient.retrofit.create(EskariakApi::class.java)
    }

    init {
        kargatuProduktuak()
    }


    private fun kargatuProduktuak() {
        egoera = egoera.copy(isLoading = true)
        produktuakApi.getProduktuak().enqueue(object : Callback<List<ProduktuaDto>> {
            override fun onResponse(call: Call<List<ProduktuaDto>>, response: Response<List<ProduktuaDto>>) {
                if (response.isSuccessful) {
                    val prodList = response.body() ?: emptyList()
                    android.util.Log.d("EskariaSortuViewModel", "Products received: ${prodList.size}")
                    prodList.forEach { p ->
                        android.util.Log.d("EskariaSortuViewModel", "Product: ${p.izena}, Mota: ${p.mota}")
                    }
                    
                    val kategoriak = prodList.map { it.mota }.distinct().sorted()
                    val kategoriaAukeratua = if (kategoriak.isNotEmpty()) kategoriak[0] else ""

                    egoera = egoera.copy(
                        produktuak = prodList,
                        kategoriak = kategoriak,
                        kategoriaAukeratua = kategoriaAukeratua,
                        isLoading = false
                    )
                } else {
                    android.util.Log.e("EskariaSortuViewModel", "Error fetching products: ${response.code()}")
                    egoera = egoera.copy(isLoading = false, error = "Errorea produktuak kargatzean")
                }
            }

            override fun onFailure(call: Call<List<ProduktuaDto>>, t: Throwable) {
                android.util.Log.e("EskariaSortuViewModel", "Network error: ${t.message}")
                egoera = egoera.copy(isLoading = false, error = "Konexio errorea: ${t.message}")
            }
        })
    }

    fun aldatuKategoria(kategoria: String) {
        egoera = egoera.copy(kategoriaAukeratua = kategoria)
    }

    fun gehituProduktua(produktuaId: Int) {
        val unekoKantitatea = egoera.saskia[produktuaId] ?: 0
        val saskiBerria = egoera.saskia.toMutableMap()
        saskiBerria[produktuaId] = unekoKantitatea + 1
        egoera = egoera.copy(saskia = saskiBerria)
    }

    fun kenduProduktua(produktuaId: Int) {
        val unekoKantitatea = egoera.saskia[produktuaId] ?: 0
        if (unekoKantitatea > 0) {
            val saskiBerria = egoera.saskia.toMutableMap()
            if (unekoKantitatea == 1) {
                saskiBerria.remove(produktuaId)
            } else {
                saskiBerria[produktuaId] = unekoKantitatea - 1
            }
            egoera = egoera.copy(saskia = saskiBerria)
        }
    }
    
    fun getProduktuaById(id: Int): ProduktuaDto? {
        return egoera.produktuak.find { it.id == id }
    }

    fun kargatuErreserbaAktiboa(mahaiaId: Int) {
        erreserbakApi.getErreserbak().enqueue(object : Callback<List<ErreserbaDto>> {
            override fun onResponse(call: Call<List<ErreserbaDto>>, response: Response<List<ErreserbaDto>>) {
                if (response.isSuccessful) {
                    val erreserbak = response.body() ?: emptyList()
                    // Filter for ALL active reservations (not just for this table)
                    val activeErreserbak = erreserbak.filter { it.ordainduta == 0 }
                    
                    // Try to pre-select one for the current table, otherwise select the first one, or null
                    val preselected = activeErreserbak.find { it.mahaiakId == mahaiaId }
                    val selectedId = preselected?.id ?: activeErreserbak.firstOrNull()?.id

                    egoera = egoera.copy(
                        erreserbak = activeErreserbak,
                        erreserbaId = selectedId
                    )
                    
                    if (activeErreserbak.isEmpty()) {
                         android.util.Log.w("EskariaSortuViewModel", "Ez da erreserba aktiborik aurkitu")
                         egoera = egoera.copy(error = "Ez dago erreserba aktiborik sisteman")
                    } else if (preselected == null && activeErreserbak.isNotEmpty()) {
                        // If we didn't find one for the specific table, but we have others, that's fine, just log it
                        android.util.Log.d("EskariaSortuViewModel", "Ez da aurkitu erreserbarik mahai honetarako ($mahaiaId), baina beste batzuk badaude.")
                    }
                } else {
                    egoera = egoera.copy(error = "Errorea erreserbak kargatzean")
                }
            }
            override fun onFailure(call: Call<List<ErreserbaDto>>, t: Throwable) {
                egoera = egoera.copy(error = "Konexio errorea: ${t.message}")
            }
        })
    }

    fun kargatuEskaria(eskariaId: Int) {
        egoera = egoera.copy(isLoading = true, error = null, eskariaId = eskariaId)

        eskariakApi.getEskaria(eskariaId).enqueue(object : Callback<EskariaDto> {
            override fun onResponse(call: Call<EskariaDto>, response: Response<EskariaDto>) {
                if (!response.isSuccessful) {
                    egoera = egoera.copy(isLoading = false, error = "Errorea eskaria kargatzean: ${response.code()}")
                    return
                }

                val eskaria = response.body()
                if (eskaria == null) {
                    egoera = egoera.copy(isLoading = false, error = "Eskaria hutsik etorri da")
                    return
                }

                val saskiBerria = eskaria.produktuak
                    ?.groupBy { it.produktuaId }
                    ?.mapValues { (_, lines) -> lines.sumOf { it.kantitatea } }
                    ?: emptyMap()

                egoera = egoera.copy(
                    eskariaId = eskaria.id,
                    erreserbaId = eskaria.erreserbaId,
                    saskia = saskiBerria
                )

                erreserbakApi.getErreserbak().enqueue(object : Callback<List<ErreserbaDto>> {
                    override fun onResponse(call: Call<List<ErreserbaDto>>, response: Response<List<ErreserbaDto>>) {
                        if (response.isSuccessful) {
                            val all = response.body() ?: emptyList()
                            val active = all.filter { it.ordainduta == 0 }
                            val selected = all.find { it.id == eskaria.erreserbaId }
                            val merged = (active + listOfNotNull(selected)).distinctBy { it.id }
                            egoera = egoera.copy(erreserbak = merged, isLoading = false)
                        } else {
                            egoera = egoera.copy(isLoading = false, error = "Errorea erreserbak kargatzean")
                        }
                    }

                    override fun onFailure(call: Call<List<ErreserbaDto>>, t: Throwable) {
                        egoera = egoera.copy(isLoading = false, error = "Konexio errorea: ${t.message}")
                    }
                })
            }

            override fun onFailure(call: Call<EskariaDto>, t: Throwable) {
                egoera = egoera.copy(isLoading = false, error = "Konexio errorea: ${t.message}")
            }
        })
    }
    
    fun aukeratuErreserba(erreserbaId: Int) {
        egoera = egoera.copy(erreserbaId = erreserbaId)
    }

    fun clearError() {
        egoera = egoera.copy(error = null)
    }

    fun clearSuccess() {
        egoera = egoera.copy(isSuccess = false, successMessage = null)
    }

    fun bidaliEskaria() {
        val erreserbaId = egoera.erreserbaId
        if (erreserbaId == null) {
            egoera = egoera.copy(error = "Ez da erreserbarik aukeratu")
            return
        }
        
        if (egoera.saskia.isEmpty()) {
            egoera = egoera.copy(error = "Saskia hutsik dago")
            return
        }

        val insufficientStock = egoera.saskia.entries.firstOrNull { (prodId, qty) ->
            val stock = getProduktuaById(prodId)?.stock ?: 0
            qty > stock
        }
        if (insufficientStock != null) {
            val prodName = getProduktuaById(insufficientStock.key)?.izena ?: "Produktu bat"
            egoera = egoera.copy(
                isLoading = false,
                error = "Ezin da eskaria egin: \"$prodName\" stock-a agortu da.",
                isSuccess = false,
                successMessage = null
            )
            return
        }
        
        egoera = egoera.copy(isLoading = true, error = null, isSuccess = false, successMessage = null)
        
        val produktuakSortu = egoera.saskia.map { (prodId, quantity) ->
            val prod = getProduktuaById(prodId)
            EskariaProduktuaSortuDto(
                produktuaId = prodId,
                kantitatea = quantity,
                prezioa = prod?.prezioa ?: 0.0
            )
        }
        
        val totala = produktuakSortu.sumOf { it.prezioa * it.kantitatea }
        
        val eskariaDto = EskariaSortuDto(
            erreserbaId = erreserbaId,
            prezioa = totala,
            egoera = "Prestatzen", 
            produktuak = produktuakSortu
        )

        val call = if (egoera.eskariaId != null) {
            eskariakApi.updateEskaria(egoera.eskariaId!!, eskariaDto)
        } else {
            eskariakApi.createEskaria(eskariaDto)
        }

        call.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    val mezua = if (egoera.eskariaId != null) "Eskaria ondo eguneratu da" else "Eskaria ondo sortu da"
                    android.util.Log.d("EskariaSortuViewModel", mezua)
                    egoera = egoera.copy(
                        saskia = emptyMap(),
                        isLoading = false,
                        eskariaId = null,
                        error = null,
                        isSuccess = true,
                        successMessage = mezua
                    )
                } else {
                    val rawError = try {
                        response.errorBody()?.string()
                    } catch (e: Exception) {
                        null
                    }
                    val mezua = mapOrderError(response.code(), rawError)
                    android.util.Log.e("EskariaSortuViewModel", "Errorea eskaria bidaltzean: ${response.code()} - ${rawError ?: "no-body"}")
                    egoera = egoera.copy(isLoading = false, error = mezua, isSuccess = false, successMessage = null)
                }
            }
            override fun onFailure(call: Call<Any>, t: Throwable) {
                android.util.Log.e("EskariaSortuViewModel", "Network error: ${t.message}")
                egoera = egoera.copy(isLoading = false, error = "Konexio errorea: ${t.message}", isSuccess = false, successMessage = null)
            }
        })
    }

    private fun mapOrderError(code: Int, rawErrorBody: String?): String {
        val extracted = extractErrorMessage(rawErrorBody)
        val lowered = extracted?.lowercase(Locale.getDefault()).orEmpty()

        if (lowered.contains("osagai") || lowered.contains("ingred")) {
            return if (extracted.isNullOrBlank()) {
                "Ezin da eskaria egin: osagaiak agortu dira."
            } else {
                "Ezin da eskaria egin: osagaiak agortu dira. ($extracted)"
            }
        }

        if (
            lowered.contains("stock") ||
            lowered.contains("existenc") ||
            lowered.contains("agot") ||
            lowered.contains("produktu") ||
            lowered.contains("producto")
        ) {
            return if (extracted.isNullOrBlank()) {
                "Ezin da eskaria egin: produktu baten stock-a agortu da."
            } else {
                "Ezin da eskaria egin: produktu baten stock-a agortu da. ($extracted)"
            }
        }

        if (!extracted.isNullOrBlank()) return extracted

        return when (code) {
            400 -> "Ezin da eskaria egin: datuak ez dira zuzenak."
            401, 403 -> "Ezin da eskaria egin: baimenik ez."
            404 -> "Ezin da eskaria egin: baliabidea ez da aurkitu."
            409 -> "Ezin da eskaria egin: stock/osagai arazoa dago."
            else -> "Errorea eskaria bidaltzean: $code"
        }
    }

    private fun extractErrorMessage(rawErrorBody: String?): String? {
        val trimmed = rawErrorBody?.trim()?.takeIf { it.isNotBlank() } ?: return null

        if (trimmed.startsWith("{")) {
            try {
                val obj = org.json.JSONObject(trimmed)
                val keys = listOf("message", "error", "title", "detail")
                for (k in keys) {
                    val v = obj.optString(k, "").trim()
                    if (v.isNotBlank()) return v
                }
            } catch (_: Exception) {
            }
        }

        return trimmed
    }
}
