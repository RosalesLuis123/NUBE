package com.example.nube

// Archivo: OpenAIApi.kt
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAIApi {
    @POST("completions")
    fun getCompletion(@Body request: OpenAIRequest): Call<OpenAIResponse>
}

