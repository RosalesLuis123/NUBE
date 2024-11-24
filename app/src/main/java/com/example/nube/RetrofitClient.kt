package com.example.nube
// Archivo: RetrofitClient.kt
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.openai.com/v1/"

    fun getOpenAIApi(apiKey: String): OpenAIApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(OpenAIApi::class.java)
    }
}

