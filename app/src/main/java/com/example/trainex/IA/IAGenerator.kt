package com.example.trainex.IA

import com.example.trainex.ejercicio.Ejercicio
import com.example.trainex.rutinas.Rutina

/**
 * Interfaz que define el contrato para los generadores de rutinas basados en IA.
 * Permite intercambiar fácilmente el motor de IA (DeepSeek, OpenAI, Gemini, etc.)
 * sin afectar al resto de la aplicación.
 */
interface IAGenerator {
    /**
     * Genera una lista de rutinas de forma asíncrona.
     * * @param ejercicios Lista base de ejercicios disponibles en la app.
     * @param numRutinas Cantidad de rutinas totales a generar.
     * @param diasPorSemana Frecuencia de entrenamiento semanal.
     * @param objetivo Meta del usuario (ej: Fuerza, Hipertrofia).
     * @param nivel Experiencia del usuario (ej: Principiante).
     * @param infoUsuario Datos adicionales del perfil del usuario para personalizar el prompt.
     * @return Result que contiene la lista de [Rutina] generadas o una excepción en caso de error.
     */
    suspend fun generarRutinas(
        ejercicios: List<Ejercicio>,
        numRutinas: Int,
        diasPorSemana: Int,
        objetivo: String,
        nivel: String,
        infoUsuario: String
    ): Result<List<Rutina>>
}