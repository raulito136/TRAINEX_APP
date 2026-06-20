package com.example.trainex.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.trainex.serie.SerieEntidad

/**
 * DAO para la gestión del historial de series de entrenamiento.
 */
@Dao
interface SerieDAO {
    /**
     * Registra o actualiza una serie de entrenamiento realizada.
     * @param serie Objeto que contiene las repeticiones, peso y el ID del ejercicio relacionado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSerie(serie: SerieEntidad)

    /**
     * Recupera el historial de series realizadas para un ejercicio específico, ordenadas secuencialmente.
     * @param ejId ID del ejercicio del cual se quieren recuperar las series.
     * @return Lista de series ordenadas por el número de serie.
     */
    @Query("SELECT * FROM historial_series WHERE ejercicioId = :ejId ORDER BY numeroSerie ASC")
    suspend fun obtenerSeriesPorEjercicio(ejId: Int): List<SerieEntidad>

    /**
     * Limpia completamente el historial de todas las series registradas en la aplicación.
     */
    @Query("DELETE FROM historial_series")
    suspend fun eliminarTodoElHistorial()
}