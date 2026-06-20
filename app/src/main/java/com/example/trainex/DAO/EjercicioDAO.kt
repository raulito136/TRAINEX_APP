package com.example.trainex.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.trainex.ejercicio.Ejercicio
import kotlinx.coroutines.flow.Flow

/**
 * DAO encargado de gestionar los datos de los ejercicios de entrenamiento.
 */
@Dao
interface EjercicioDAO {
    /**
     * Inserta múltiples ejercicios. Si el ejercicio ya existe, reemplaza los datos antiguos.
     * @param ejercicios Lista de objetos [Ejercicio] a persistir.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(ejercicios: List<Ejercicio>)

    /**
     * Obtiene todos los ejercicios en forma de [Flow] para actualizaciones reactivas en la UI.
     * @return Un flujo reactivo con la lista completa de ejercicios.
     */
    @Query("SELECT * FROM ejercicios")
    fun obtenerTodos(): Flow<List<Ejercicio>>

    /**
     * Filtra ejercicios según el grupo muscular objetivo.
     * @param grupo Clave del grupo muscular (ej: "chest", "legs").
     * @return Lista de ejercicios que trabajan ese grupo.
     */
    @Query("SELECT * FROM ejercicios WHERE grupoMuscular = :grupo")
    suspend fun obtenerPorGrupoMuscular(grupo: String): List<Ejercicio>

    /**
     * Busca un ejercicio específico mediante su identificador único.
     * @param id Identificador del ejercicio.
     * @return El ejercicio encontrado o null si no existe.
     */
    @Query("SELECT * FROM ejercicios WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Ejercicio?
}