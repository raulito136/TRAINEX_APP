package com.example.trainex.IA

import android.content.Context
import android.util.Log
import com.example.trainex.ejercicio.Ejercicio
import com.example.trainex.rutinas.Rutina
import com.example.trainex.BuildConfig
import com.example.trainex.R
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Generador de rutinas de entrenamiento utilizando la API de DeepSeek.
 *
 * Esta clase implementa [IAGenerator] y se encarga de:
 * 1. Validar los datos de entrada del usuario.
 * 2. Construir un prompt detallado para la IA.
 * 3. Ejecutar la petición HTTP a la API.
 * 4. Parsear la respuesta JSON y convertirla en objetos internos [Rutina] y [Ejercicio].
 *
 * @param context Contexto de la aplicación, necesario para acceder a assets y recursos.
 * @param client Cliente HTTP configurable (por defecto se crea uno con timeouts).
 * @param gson Instancia de Gson para parsear JSON.
 */
class DeepSeekGenerator(
    private val context: Context,
    private val client: OkHttpClient = defaultOkHttpClient(),
    private val gson: Gson = Gson()
) : IAGenerator {

    companion object {
        private const val API_URL = "https://api.deepseek.com/chat/completions" // Endpoint de la API
        private const val TIMEOUT_SECONDS = 100L // Timeout de conexión y lectura
        private const val MAX_RUTINAS = 7       // Número máximo de rutinas que se pueden generar

        /**
         * Genera un cliente OkHttp con timeouts configurados.
         */
        private fun defaultOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build()
        }
    }

    // ----------- MODELOS PARA PARSEO DE LA RESPUESTA DE LA API ----------
    /**
     * Representa un ejercicio tal como lo devuelve la API.
     */
    data class EjercicioApi(
        val nombre: String,
        val series: Int,
        val repeticiones: String
    )

    /**
     * Contenedor de las rutinas devueltas por la API.
     */
    data class RutinasResponse(
        val rutinas: List<RutinaApi>
    )

    /**
     * Representa una rutina diaria devuelta por la API.
     */
    data class RutinaApi(
        val dia: Int,
        @SerializedName("grupo_muscular") val grupoMuscular: String,
        val ejercicios: List<EjercicioApi>
    )
    // -------------------------------------------------------------------

    /**
     * Genera rutinas de entrenamiento usando la IA.
     *
     * Valida los inputs, ejecuta la petición HTTP y parsea la respuesta.
     *
     * @param ejercicios Lista de ejercicios disponibles.
     * @param numRutinas Número de rutinas a generar.
     * @param dias Número de días de entrenamiento por semana.
     * @param objetivo Objetivo del usuario (hipertrofia, fuerza, resistencia, pérdida de grasa).
     * @param nivel Nivel de experiencia del usuario (principiante, intermedio, avanzado).
     * @param infoUsuario Información adicional del usuario.
     * @return Resultado con la lista de rutinas o un error en caso de fallo.
     */
    override suspend fun generarRutinas(
        ejercicios: List<Ejercicio>,
        numRutinas: Int,
        dias: Int,
        objetivo: String,
        nivel: String,
        infoUsuario: String
    ): Result<List<Rutina>> {

        return try {
            // Validar los inputs del usuario
            validateInputs(ejercicios, numRutinas, dias, objetivo, nivel)

            // Verificar que la API Key está configurada
            if (BuildConfig.DEEPSEEK_API_KEY.isBlank()) {
                throw IllegalStateException("API Key no configurada")
            }

            // Ejecutar la petición HTTP a la API
            val response = executeApiRequest(ejercicios, numRutinas, dias, objetivo, nivel, infoUsuario)

            // Parsear la respuesta y convertirla a objetos internos
            parseApiResponse(response, numRutinas, ejercicios)

        } catch (e: Exception) {
            Log.e("DeepSeekGenerator", "Error generando rutina IA: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- VALIDACIÓN DE ENTRADAS ---
    private fun validateInputs(
        ejercicios: List<Ejercicio>,
        numRutinas: Int,
        dias: Int,
        objetivo: String,
        nivel: String
    ) {
        // Validaciones básicas
        require(ejercicios.isNotEmpty()) { "La lista de ejercicios no puede estar vacía" }
        require(numRutinas in 1..MAX_RUTINAS)
        require(dias in 1..7)

        // Convertimos a minúsculas para una comparación más robusta
        val objetivoLower = objetivo.lowercase()

        // Definimos las palabras clave que aceptaremos
        val esValido = when {
            objetivoLower.contains("hipertrofia") || objetivoLower.contains("hypertrophy") -> true
            objetivoLower.contains("fuerza") || objetivoLower.contains("strength") -> true
            objetivoLower.contains("resistencia") || objetivoLower.contains("endurance") -> true
            objetivoLower.contains("grasa") || objetivoLower.contains("fat") || objetivoLower.contains("loss") -> true
            else -> false
        }

        if (!esValido) {
            throw IllegalArgumentException("Objetivo no válido: '$objetivo'")
        }

        // Hacemos lo mismo para el nivel
        val nivelLower = nivel.lowercase()
        val nivelEsValido = nivelLower.contains("principiante") || nivelLower.contains("beginner") ||
                nivelLower.contains("intermedio") || nivelLower.contains("intermediate") ||
                nivelLower.contains("avanzado") || nivelLower.contains("advanced")

        if (!nivelEsValido) {
            throw IllegalArgumentException("Nivel no válido: '$nivel'")
        }
    }

    // --- PETICIÓN A LA API ---
    /**
     * Ejecuta la llamada HTTP a la API en un contexto de IO.
     */
    private suspend fun executeApiRequest(
        ejercicios: List<Ejercicio>,
        numRutinas: Int,
        dias: Int,
        objetivo: String,
        nivel: String,
        infoUsuario: String
    ): Response {
        return withContext(Dispatchers.IO) {
            client.newCall(
                createApiRequest(ejercicios, numRutinas, dias, objetivo, nivel, infoUsuario)
            ).execute()
        }
    }

    /**
     * Construye el Request HTTP con el prompt para la IA.
     */
    private fun createApiRequest(
        ejercicios: List<Ejercicio>,
        numRutinas: Int,
        dias: Int,
        objetivo: String,
        nivel: String,
        infoUsuario: String
    ): Request {

        // Carga la lista de ejercicios desde JSON local
        val jsonString = context.assets.open("ejercicios.json").bufferedReader().use { it.readText() }
        val ejerciciosList = gson.fromJson(jsonString, Array<Ejercicio>::class.java).toList()
        Log.d("DeepSeekGenerator", "Cantidad de ejercicios cargados desde JSON: ${ejerciciosList.size}")
        Log.d("DeepSeekGenerator", "$nivel, $objetivo, $infoUsuario, $dias")
        val nombresEjercicios = ejerciciosList.map { it.nombre }.joinToString(", ")
        val idiomaActual = com.example.trainex.utils.LanguageUtils.getIdiomaActual(context)
        val nombreIdioma = if (idiomaActual == "en") "English" else "Spanish"
        val prompt = """
Eres un entrenador personal. Genera $numRutinas rutinas, una por día de entrenamiento, para el siguiente usuario:

- Usuario: $infoUsuario
- Días por semana: $dias
- Objetivo: $objetivo
- Nivel: $nivel


Usa solo estos ejercicios: $nombresEjercicios

IMPORTANTE:
- Usa exclusivamente los ejercicios proporcionados en la lista. 
- Está completamente prohibido modificar, corregir, resumir o inventar nombres. 
- El campo "nombre" debe copiarse **exactamente** como aparece en la lista, sin cambiar una sola letra.
- Respeta mayúsculas, minúsculas, tildes, paréntesis, guiones o cualquier otro signo tal como están escritos.
- No escribas variantes del nombre. Si el ejercicio no aparece **exactamente igual**, no lo uses.
- Ejemplos de lo que NO debes hacer:
    - ❌ "Dominada (Con peso)" → ✅ "Dominada (Con Peso Agregado)"
    - ❌ "Extension de Gemelos de Pie (Máquina)" → ✅ "Extension de Pantorrilla (Máquina)"
    - ❌ "Extension de Triceps" → ✅ "Extensión de Tríceps"
- No hay límite en el número de ejercicios por día. Usa los que sean necesarios para una rutina completa, según el nivel y objetivo del usuario.
- Asegúrate de cubrir todos los grupos musculares si los días lo permiten.

IMPORTANTE SOBRE LOS CAMPOS:
- El campo "grupo_muscular" debe contener ÚNICAMENTE los músculos trabajados.
- NO incluyas el nivel (principiante, avanzado, etc.) en el nombre del grupo muscular.
- NO incluyas el tipo de rutina (cuerpo completo, split, etc.) a menos que sea estrictamente el nombre del grupo.
- Ejemplo de lo que NO debes hacer: "Pecho (Principiante)" o "Full Body (1 día)".
- Ejemplo de lo que SÍ debes hacer: "Pecho y Tríceps" o "Cuerpo Completo".
- El campo "grupo_muscular" debe estar redactado en $nombreIdioma (ejemplo en inglés: "Full Body", "Chest and Triceps"; ejemplo en español: "Cuerpo Completo", "Pecho y Tríceps").
- NO incluyas el nivel ni el tipo de rutina en el nombre del grupo muscular.

Devuelve la respuesta en formato JSON exactamente así:

{
  "rutinas": [
    {
      "dia": 1,
      "grupo_muscular": "${if (idiomaActual == "en") "Chest and Triceps" else "Pecho y Tríceps"}",
      "ejercicios": [
        {"nombre": "Press de Banca (Barra)", "series": 4, "repeticiones": "8-10"},
        {"nombre": "Fondos en paralelas", "series": 3, "repeticiones": "10-12"},
        {"nombre": "Aperturas con mancuernas", "series": 3, "repeticiones": "12-15"}
      ]
    }
  ]
}

Devuelve exactamente $numRutinas rutinas, sin explicaciones, solo el JSON.
""".trimIndent()

        val jsonBody = gson.toJson(
            mapOf(
                "model" to "deepseek-chat",
                "messages" to listOf(mapOf("role" to "user", "content" to prompt)),
                "temperature" to 0.7
            )
        )

        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

        return Request.Builder()
            .url(API_URL)
            .post(requestBody)
            .addHeader("Authorization", "Bearer ${BuildConfig.DEEPSEEK_API_KEY}")
            .build()
    }

    /**
     * Limpia posibles backticks de la respuesta JSON.
     */
    private fun cleanJsonString(raw: String): String =
        raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

    // --- PARSEO DE RESPUESTA ---
    /**
     * Convierte la respuesta de la API en una lista de [Rutina] con sus [Ejercicio].
     */
    /**
     * Convierte la respuesta de la API en una lista de [Rutina] con sus [Ejercicio].
     * Se ha mejorado la búsqueda para ser insensible a mayúsculas y espacios.
     */
    private fun parseApiResponse(
        response: Response,
        expectedRutinas: Int,
        listaEjerciciosOriginal: List<Ejercicio>
    ): Result<List<Rutina>> {

        if (!response.isSuccessful) throw IOException("Respuesta no exitosa: ${response.code}")

        val body = response.body?.string() ?: throw IOException("Respuesta vacía")
        Log.d("DeepSeekGenerator", "Respuesta cruda: $body")

        // Extrae el contenido del assistant (navegando por la estructura de DeepSeek/OpenAI)
        val content = gson.fromJson(body, Map::class.java)["choices"]
            ?.let { it as List<*> }
            ?.firstOrNull()
            ?.let { it as Map<*, *> }
            ?.get("message")
            ?.let { it as Map<*, *> }
            ?.get("content") as? String
            ?: throw IOException("Respuesta sin campo 'content'")

        val clean = cleanJsonString(content)
        Log.d("DeepSeekGenerator", "JSON limpio:\n$clean")

        // Parseamos el JSON limpio al objeto contenedor RutinasResponse
        val parsed: RutinasResponse = try {
            gson.fromJson(clean, RutinasResponse::class.java)
        } catch (e: JsonSyntaxException) {
            throw IOException("JSON inválido: $clean", e)
        }

        // Convertimos los modelos de la API a nuestros objetos internos de la App
        val rutinasList = parsed.rutinas.map { rutinaApi ->
            // Usamos mapNotNull para poder filtrar los que devolvamos como null
            val ejerciciosConvertidos = rutinaApi.ejercicios.mapNotNull { ejApi ->

                // --- MEJORA DE BÚSQUEDA ROBUSTA ---
                val nombreBusqueda = ejApi.nombre.trim()

                val original = listaEjerciciosOriginal.find {
                    it.nombre.trim().equals(nombreBusqueda, ignoreCase = true)
                }

                // 1. Si no se encuentra en el JSON, lo omitimos
                if (original == null) {
                    Log.e("DeepSeekGenerator", "ERROR: No se encontró el ejercicio '${ejApi.nombre}' en el JSON local. Se omite.")
                    return@mapNotNull null
                }

                // 2. Si se encuentra pero no tiene descripción, lo omitimos
                if (original.descripcion.isBlank()) {
                    Log.w("DeepSeekGenerator", "ADVERTENCIA: El ejercicio '${original.nombre}' no tiene descripción. Se omite.")
                    return@mapNotNull null
                }

                // Creamos el objeto Ejercicio ya que pasó las validaciones
                Ejercicio(
                    id = original.id,
                    nombre = original.nombre,
                    grupoMuscular = original.grupoMuscular,
                    imagen = original.imagen,
                    descripcion = original.descripcion,
                    dificultad = original.dificultad,
                    urlVideo = original.urlVideo
                )
            }

            // Construimos el objeto Rutina final
            Rutina(
                id = System.currentTimeMillis() + rutinaApi.dia,
                titulo = context.getString(R.string.dia, rutinaApi.dia, rutinaApi.grupoMuscular),
                ejercicios = ArrayList(ejerciciosConvertidos),
                userId = ""
            )
        }

        // Validación final de cantidad
        if (rutinasList.size != expectedRutinas) {
            throw IllegalStateException("Se esperaban $expectedRutinas rutinas pero se recibieron ${rutinasList.size}")
        }

        return Result.success(rutinasList)
    }
}
