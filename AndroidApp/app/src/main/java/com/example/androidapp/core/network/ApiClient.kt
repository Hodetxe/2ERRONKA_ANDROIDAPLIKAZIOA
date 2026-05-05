package com.example.androidapp.core.network

import android.content.Context
import com.example.androidapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val PREFS = "api_client_prefs"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_CHAT_HOST = "chat_host"
    private const val KEY_CHAT_PORT = "chat_port"

    private var baseUrlOverride: String? = null
    private var chatHostOverride: String? = null
    private var chatPortOverride: Int? = null

    val BASE_URL: String
        get() = baseUrlOverride ?: BuildConfig.API_BASE_URL

    val CHAT_HOST: String
        get() = chatHostOverride ?: BuildConfig.CHAT_HOST

    val CHAT_PORT: Int
        get() = chatPortOverride ?: BuildConfig.CHAT_PORT

    private val loginEgiten by lazy {
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    }

    private val httpEskaera by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loginEgiten)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .callTimeout(25, TimeUnit.SECONDS)
            .build()
    }

    private var retrofitInstance: Retrofit? = null

    val retrofit: Retrofit
        get() = synchronized(this) {
            retrofitInstance ?: Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpEskaera)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .also { retrofitInstance = it }
        }

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        baseUrlOverride = prefs.getString(KEY_BASE_URL, null)
            ?.takeIf { it.isNotBlank() }
            ?.let { normalizeBaseUrl(it) }
        chatHostOverride = prefs.getString(KEY_CHAT_HOST, null)?.takeIf { it.isNotBlank() }
        chatPortOverride = prefs.getInt(KEY_CHAT_PORT, -1).takeIf { it > 0 }
    }

    fun updateSettings(
        context: Context,
        apiBaseUrl: String,
        chatHost: String,
        chatPort: Int
    ) {
        val normalizedBaseUrl = normalizeBaseUrl(apiBaseUrl)

        baseUrlOverride = normalizedBaseUrl
        chatHostOverride = chatHost.trim().ifBlank { BuildConfig.CHAT_HOST }
        chatPortOverride = chatPort

        synchronized(this) {
            retrofitInstance = null
        }

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_BASE_URL, baseUrlOverride)
            .putString(KEY_CHAT_HOST, chatHostOverride)
            .putInt(KEY_CHAT_PORT, chatPortOverride ?: BuildConfig.CHAT_PORT)
            .apply()
    }

    private fun normalizeBaseUrl(input: String): String {
        val trimmed = input.trim()
        val withScheme = when {
            trimmed.isBlank() -> BuildConfig.API_BASE_URL
            trimmed.startsWith("http://", ignoreCase = true) -> trimmed
            trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            else -> "http://$trimmed"
        }

        return if (withScheme.endsWith("/")) withScheme else "$withScheme/"
    }
}
