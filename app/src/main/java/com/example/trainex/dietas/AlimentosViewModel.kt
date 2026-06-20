package com.example.trainex.dietas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainex.network.RetrofitClient
import com.example.trainex.repository.FirebaseAlimentosRepository
import com.example.trainex.utils.TraductorUniversal
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*

/**
 * ViewModel que gestiona la búsqueda y gestión de alimentos.
 * Implementa una búsqueda híbrida entre Firebase Global y la API de OpenFoodFacts.
 */
class AlimentosViewModel(private val firebaseRepo: FirebaseAlimentosRepository) : ViewModel() {

    private val _listaAlimentos = MutableLiveData<List<Alimento>>()
    val listaAlimentos: LiveData<List<Alimento>> get() = _listaAlimentos

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private var listaMisAlimentosCache: List<Alimento> = emptyList()
    private var searchJob: Job? = null
    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /**
     * Carga y traduce los alimentos personales del usuario desde Firebase.
     * @param idioma Código del idioma para la traducción (es/en).
     */
    fun cargarMisAlimentos(idioma: String) {
        _isLoading.value = true
        _listaAlimentos.value = emptyList()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                TraductorUniversal.configurarIdioma(idioma)
                val raw = firebaseRepo.obtenerMisAlimentos(currentUserId)

                val traducidos = raw.map { item ->
                    item.copy(
                        nombre = TraductorUniversal.traducirSincrono(item.nombre),
                        marca = TraductorUniversal.traducirSincrono(item.marca),
                        categoria = TraductorUniversal.traducirSincrono(item.categoria)
                    )
                }

                listaMisAlimentosCache = traducidos
                withContext(Dispatchers.Main) {
                    _listaAlimentos.value = traducidos
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage)
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Decide si filtrar la caché local o realizar una búsqueda en internet.
     */
    fun iniciarBusqueda(texto: String?, pestañaActual: Int, idioma: String) {
        val query = texto?.trim()?.lowercase() ?: ""
        searchJob?.cancel()

        if (pestañaActual == 1) { // Pestaña "Mis Alimentos"
            _listaAlimentos.value = if (query.isEmpty()) {
                listaMisAlimentosCache
            } else {
                listaMisAlimentosCache.filter {
                    it.nombre.lowercase().contains(query) || it.marca.lowercase().contains(query)
                }
            }
        } else { // Pestaña "Buscar"
            searchJob = viewModelScope.launch {
                if (query.isNotEmpty()) delay(600)
                val termino = if (query.isEmpty()) "arroz" else query
                buscarEnInternet(termino, idioma)
            }
        }
    }

    /**
     * Ejecuta búsquedas concurrentes en Firebase Global y OpenFoodFacts.
     */
    private suspend fun buscarEnInternet(query: String, idioma: String) {
        withContext(Dispatchers.Main) { _isLoading.value = true }

        try {
            coroutineScope {
                TraductorUniversal.configurarIdioma(idioma)

                val jobFirebase = async(Dispatchers.IO) {
                    firebaseRepo.buscarAlimentosGlobales(query)
                }

                val jobApi = async(Dispatchers.IO) {
                    val queryApi = if (idioma == "es") {
                        val terminoIngles = TraductorUniversal.traducirEspañolAIngles(query)
                        if (!terminoIngles.equals(query, true)) "$query $terminoIngles" else query
                    } else query

                    val pais = if (idioma == "es") "es" else "us"
                    val response = RetrofitClient.instance.buscarAlimentos(queryApi, idioma, pais)

                    if (response.isSuccessful) response.body()?.products?.filter { !it.productName.isNullOrBlank() } ?: emptyList()
                    else emptyList()
                }

                val resultadosFirebase = jobFirebase.await()
                val resultadosApiRaw = jobApi.await()

                val resultadosApiProcesados = resultadosApiRaw.map { prod ->
                    async(Dispatchers.IO) {
                        try {
                            val nombreTrad = TraductorUniversal.traducirSincrono(prod.productName!!.trim())
                            val catRaw = prod.categories?.split(",")?.firstOrNull()?.trim()?.substringAfter(":") ?: "General"
                            val catTrad = TraductorUniversal.traducirSincrono(catRaw)

                            Alimento(
                                firebaseId = "off_${prod.productName!!.lowercase().hashCode()}",
                                nombre = nombreTrad.replaceFirstChar { it.uppercase() },
                                marca = TraductorUniversal.traducirSincrono(prod.brands ?: "Genérico"),
                                imagen = prod.imageSmallUrl ?: prod.imageUrl ?: "https://cdn-icons-png.flaticon.com/512/2515/2515183.png",
                                categoria = catTrad,
                                calorias = prod.nutriments?.kcal100g?.toInt() ?: 0,
                                proteinas = prod.nutriments?.proteins100g ?: 0.0,
                                carbohidratos = prod.nutriments?.carbs100g ?: 0.0,
                                grasas = prod.nutriments?.fat100g ?: 0.0,
                                esLiquido = prod.unit?.equals("ml", true) == true
                            )
                        } catch (e: Exception) { null }
                    }
                }.awaitAll().filterNotNull()

                withContext(Dispatchers.Main) {
                    _listaAlimentos.value = resultadosFirebase + resultadosApiProcesados
                    _isLoading.value = false
                }
            }
        } catch (e: Exception) {
            _isLoading.postValue(false)
            _error.postValue(e.message)
        }
    }

    /**
     * Elimina un alimento personal de Firebase y de la caché.
     */
    fun eliminarAlimento(alimento: Alimento) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firebaseRepo.eliminarAlimento(currentUserId, alimento)
                listaMisAlimentosCache = listaMisAlimentosCache.filter { it.firebaseId != alimento.firebaseId }

                withContext(Dispatchers.Main) {
                    _listaAlimentos.value = listaMisAlimentosCache
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.postValue(false)
                _error.postValue(e.localizedMessage)
            }
        }
    }
}