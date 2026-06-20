package com.example.trainex.utils

import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Objeto de utilidad que gestiona la traducción automática y la identificación de idiomas.
 * Utiliza Google ML Kit para procesar textos de forma local (on-device).
 * Implementa un sistema de caché de traductores para optimizar el rendimiento.
 */
object TraductorUniversal {

    /** Caché de traductores para diferentes pares de idiomas. */
    private val traductoresCache = ConcurrentHashMap<String, Translator>()

    /** Cliente de ML Kit para la identificación del idioma de un texto. */
    private val identificador = LanguageIdentification.getClient()

    /** Traductor específico optimizado para procesos de búsqueda (Español -> Inglés). */
    private var traductorBusqueda: Translator? = null

    /** Variable persistente que almacena el idioma seleccionado manualmente por el usuario en la App. */
    private var idiomaManual: String? = null

    /**
     * Configura el idioma de destino de la aplicación.
     * Si el idioma cambia, limpia la caché de traductores existentes.
     * @param nuevoIdioma El código ISO del nuevo idioma (ej: "es", "en").
     */
    fun configurarIdioma(nuevoIdioma: String) {
        if (idiomaManual != nuevoIdioma) {
            idiomaManual = nuevoIdioma
            traductoresCache.values.forEach { it.close() }
            traductoresCache.clear()

            traductorBusqueda?.close()
            traductorBusqueda = null
        }
    }

    /**
     * Determina el idioma actual de la aplicación (el manual o el del sistema).
     */
    private val idiomaApp: String
        get() = idiomaManual ?: Locale.getDefault().language

    /**
     * Define dinámicamente el idioma destino para el motor de ML Kit.
     */
    private val targetLangMLKit: String
        get() = if (idiomaApp == "es") TranslateLanguage.SPANISH else TranslateLanguage.ENGLISH

    /**
     * Traduce un texto de forma síncrona bloqueando el hilo actual.
     * Incluye lógica de identificación automática y descarga de modelos si es necesario.
     * @param textoOriginal El texto que se desea traducir.
     * @return El texto traducido con la primera letra en mayúscula o el original si ocurre un error.
     */
    fun traducirSincrono(textoOriginal: String): String {
        if (textoOriginal.isBlank()) return ""

        val textoLimpio = textoOriginal.trim()

        try {
            // Identificar idioma de origen
            val taskIdentificacion = identificador.identifyLanguage(textoLimpio)
            var codigoIdiomaOrigen = try {
                Tasks.await(taskIdentificacion, 3, TimeUnit.SECONDS)
            } catch (e: Exception) { "und" }

            // Lógica para palabras cortas o idiomas no identificados
            if (codigoIdiomaOrigen == "und" || codigoIdiomaOrigen == "zxx") {
                codigoIdiomaOrigen = if (idiomaApp == "en") "es" else "en"
            }

            // Si el texto ya está en el idioma de la App, no traducir
            if (codigoIdiomaOrigen == idiomaApp) return textoOriginal

            // Obtención del cliente de traducción
            val traductor = obtenerTraductor(codigoIdiomaOrigen) ?: return textoOriginal

            // Gestión de descarga de modelos (máximo 20 segundos)
            val condiciones = DownloadConditions.Builder().build()
            try {
                Tasks.await(traductor.downloadModelIfNeeded(condiciones), 20, TimeUnit.SECONDS)
            } catch (e: Exception) {
                return textoOriginal
            }

            // Ejecución de la traducción
            return Tasks.await(traductor.translate(textoLimpio), 5, TimeUnit.SECONDS)
                .replaceFirstChar { it.uppercase() }

        } catch (e: Exception) {
            return textoOriginal
        }
    }

    /**
     * Traduce específicamente del Español al Inglés.
     * Útil para estandarizar términos antes de realizar peticiones a APIs externas.
     * @param texto El texto en español.
     * @return El texto traducido al inglés.
     */
    fun traducirEspañolAIngles(texto: String): String {
        try {
            if (traductorBusqueda == null) {
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.SPANISH)
                    .setTargetLanguage(TranslateLanguage.ENGLISH)
                    .build()
                traductorBusqueda = Translation.getClient(options)
                val condiciones = DownloadConditions.Builder().build()
                Tasks.await(traductorBusqueda!!.downloadModelIfNeeded(condiciones), 5, TimeUnit.SECONDS)
            }
            return Tasks.await(traductorBusqueda!!.translate(texto), 2, TimeUnit.SECONDS)
        } catch (e: Exception) {
            return texto
        }
    }

    /**
     * Identifica el idioma de un texto dado.
     * @param texto El texto a analizar.
     * @return El código ISO del idioma o "und" si es indeterminado.
     */
    fun identificarIdioma(texto: String): String {
        return try {
            Tasks.await(identificador.identifyLanguage(texto), 1, TimeUnit.SECONDS)
        } catch (e: Exception) { "und" }
    }

    /**
     * Valida si un texto pertenece a un idioma aceptable por la aplicación
     * (el idioma configurado o indeterminado para marcas comerciales).
     * @param texto El texto a validar.
     * @return true si el idioma es válido para mostrar al usuario.
     */
    fun esIdiomaValido(texto: String): Boolean {
        try {
            val codigo = Tasks.await(identificador.identifyLanguage(texto), 1, TimeUnit.SECONDS)
            return codigo == idiomaApp || codigo == "und"
        } catch (e: Exception) { return true }
    }

    /**
     * Obtiene un traductor de la caché o crea uno nuevo según el idioma de origen.
     * Método sincronizado para evitar condiciones de carrera en el acceso a la caché.
     */
    @Synchronized
    private fun obtenerTraductor(idiomaOrigen: String): Translator? {
        if (traductoresCache.containsKey(idiomaOrigen)) return traductoresCache[idiomaOrigen]

        val sourceLang = TranslateLanguage.fromLanguageTag(idiomaOrigen) ?: return null

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLangMLKit)
            .build()

        val nuevoTraductor = Translation.getClient(options)
        traductoresCache[idiomaOrigen] = nuevoTraductor
        return nuevoTraductor
    }

    /**
     * Descarga los modelos de traducción necesarios de forma asíncrona al iniciar la App.
     * Prepara los modelos Español e Inglés para que estén disponibles sin conexión.
     * @param onListo Callback opcional que se ejecuta al finalizar la preparación.
     */
    fun preparar(onListo: () -> Unit = {}) {
        Thread {
            try {
                val idiomas = listOf(TranslateLanguage.SPANISH, TranslateLanguage.ENGLISH)
                val condiciones = DownloadConditions.Builder().build()

                idiomas.forEach { lang ->
                    val options = TranslatorOptions.Builder()
                        .setSourceLanguage(lang)
                        .setTargetLanguage(if (lang == TranslateLanguage.ENGLISH) TranslateLanguage.SPANISH else TranslateLanguage.ENGLISH)
                        .build()
                    Tasks.await(Translation.getClient(options).downloadModelIfNeeded(condiciones), 30, TimeUnit.SECONDS)
                }
            } catch (e: Exception) { }
            onListo()
        }.start()
    }
}