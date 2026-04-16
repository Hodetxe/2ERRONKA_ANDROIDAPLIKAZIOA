package com.example.androidapp.data.remote

import com.example.androidapp.data.dto.LangileaDto
import com.example.androidapp.data.dto.LoginErantzuna
import com.example.androidapp.data.dto.LoginEskaera
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApi {
    @POST("api/Login")
    fun login(@Body request: LoginEskaera): Call<LoginErantzuna>
}
