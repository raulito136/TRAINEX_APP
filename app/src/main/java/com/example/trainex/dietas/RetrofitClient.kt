package com.example.trainex.network

import OpenFoodFactsApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Singleton para centralizar la configuración de la red.
 */
object RetrofitClient {
    // URL base de la API mundial de Open Food Facts.
    private const val BASE_URL = "https://world.openfoodfacts.org/"

    // Configuración del cliente HTTP (tiempos de espera).
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // Tiempo máximo para conectar.
        .readTimeout(60, TimeUnit.SECONDS)    // Tiempo máximo para recibir datos.
        .build()

    /**
     * Instancia de la API creada de forma perezosa (lazy).
     * Solo se crea la primera vez que se accede a ella.
     */
    val instance: OpenFoodFactsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            // GsonConverterFactory convierte automáticamente el JSON a nuestras clases DTO.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsApi::class.java)
    }
}