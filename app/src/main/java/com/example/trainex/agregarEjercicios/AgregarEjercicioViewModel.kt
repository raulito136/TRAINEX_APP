package com.example.trainex.agregarEjercicios

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainex.database.AppDatabase
import com.example.trainex.ejercicio.Ejercicio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de la lógica de selección de ejercicios.
 * Gestiona el filtrado reactivo de datos y el estado de la selección múltiple.
 */
class AgregarEjercicioViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val context = application.applicationContext

    private val _allExercises = MutableStateFlow<List<Ejercicio>>(emptyList())
    private val _searchText = MutableStateFlow("")
    private val _selectedGroup = MutableStateFlow<String?>(null)

    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIds: StateFlow<Set<Int>> = _selectedIds

    private val _filteredExercises = MutableStateFlow<List<Ejercicio>>(emptyList())
    val filteredExercises: StateFlow<List<Ejercicio>> = _filteredExercises

    private val _gruposParaFiltro = MutableStateFlow<List<String>>(listOf("Todos"))
    val gruposParaFiltro: StateFlow<List<String>> = _gruposParaFiltro

    init {
        fetchExercises()
        observeFilters()
    }

    /**
     * Carga los ejercicios desde la base de datos y calcula los grupos musculares únicos.
     */
    private fun fetchExercises() {
        viewModelScope.launch {
            database.ejercicioDao().obtenerTodos().collect { lista ->
                _allExercises.value = lista
                if (lista.isNotEmpty()) {
                    val gruposUnicos = listOf("Todos") + lista.map { it.grupoMuscular }.distinct().sorted()
                    _gruposParaFiltro.value = gruposUnicos
                }
                aplicarFiltros()
            }
        }
    }

    /**
     * Combina los flujos de texto, grupo y lista total para emitir una nueva lista filtrada.
     */
    private fun observeFilters() {
        viewModelScope.launch {
            combine(_searchText, _selectedGroup, _allExercises) { text, group, list ->
                Triple(text, group, list)
            }.collect { (text, group, list) ->
                val filtered = list.filter { ejercicio ->
                    val nombreVisual = obtenerNombreTraducido(ejercicio.nombre)
                    val coincideTexto = if (text.isEmpty()) true else
                        nombreVisual.contains(text, ignoreCase = true)

                    val coincideGrupo = if (group == null) true else
                        ejercicio.grupoMuscular == group

                    coincideTexto && coincideGrupo
                }
                _filteredExercises.value = filtered
            }
        }
    }

    /**
     * Actualiza el texto de búsqueda actual.
     */
    fun updateSearchText(query: String) {
        _searchText.value = query
    }

    /**
     * Actualiza el grupo muscular seleccionado para filtrar.
     */
    fun updateSelectedGroup(grupo: String?) {
        _selectedGroup.value = grupo
    }

    /**
     * Alterna el estado de selección de un ejercicio por su ID.
     */
    fun toggleSelection(ejercicioId: Int) {
        val currentSet = _selectedIds.value.toMutableSet()
        if (currentSet.contains(ejercicioId)) {
            currentSet.remove(ejercicioId)
        } else {
            currentSet.add(ejercicioId)
        }
        _selectedIds.value = currentSet
    }

    /**
     * Establece los ejercicios que ya estaban seleccionados previamente.
     */
    fun setPreselected(lista: List<Ejercicio>) {
        _selectedIds.value = lista.map { it.id }.toSet()
    }

    /**
     * Obtiene la lista completa de objetos [Ejercicio] seleccionados.
     */
    fun getSelectedExercisesList(): ArrayList<Ejercicio> {
        return ArrayList(_allExercises.value.filter { _selectedIds.value.contains(it.id) })
    }

    /**
     * Traduce una clave de recurso string a su valor de texto real según el idioma.
     */
    private fun obtenerNombreTraducido(key: String): String {
        val resId = context.resources.getIdentifier(key, "string", context.packageName)
        return if (resId != 0) context.getString(resId) else key
    }

    private fun aplicarFiltros() {
        updateSearchText(_searchText.value)
    }
}