package com.example.androidapp.core.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    const val BASE_URL = "http://127.0.0.1:5000/"
    //const val BASE_URL = "http://172.16.242.226:5000/"
    //const val BASE_URL = "http://192.168.1.112:5000/"
    //const val BASE_URL = "http://192.168.1.202:5000/"

    private const val Api_helbidea = BASE_URL
    private val loginEgiten by lazy {
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    }

    private val httpEskaera by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loginEgiten)
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Api_helbidea)
            .client(httpEskaera)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
