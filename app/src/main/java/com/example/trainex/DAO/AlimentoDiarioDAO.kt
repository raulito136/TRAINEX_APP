package com.example.trainex.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.trainex.dietas.AlimentoDiario

/**
 * Interfaz de Acceso a Datos (DAO) para la entidad [AlimentoDiario].
 * Gestiona la persistencia de los alimentos consumidos en la tabla "diario_comidas".
 */
@Dao
interface AlimentoDiarioDAO {
    /**
     * Inserta un nuevo registro de consumo de alimento en la base de datos.
     * @param registro Objeto con la información del alimento y la fecha.
     */
    @Insert
    suspend fun insertarRegistro(registro: AlimentoDiario)

    /**
     * Actualiza los datos de un registro de comida ya existente.
     * @param registro Objeto con los datos actualizados.
     */
    @Update
    suspend fun actualizarRegistro(registro: AlimentoDiario)

    /**
     * Recupera una lista de alimentos consumidos filtrados por fecha y tipo de comida.
     * @param fecha Cadena de texto que representa la fecha del registro.
     * @param tipo Categoría de la comida (ej: Desayuno, Almuerzo).
     * @return Lista de [AlimentoDiario] que coinciden con los criterios.
     */
    @Query("SELECT * FROM diario_comidas WHERE fecha = :fecha AND tipoComida = :tipo")
    suspend fun obtenerComidasPorTipo(fecha: String, tipo: String): List<AlimentoDiario>

    /**
     * Elimina un registro específico del diario mediante su identificador único.
     * @param id Identificador numérico del registro a borrar.
     */
    @Query("DELETE FROM diario_comidas WHERE id = :id")
    suspend fun eliminarRegistro(id: Int)

    /**
     * Borra permanentemente todos los registros almacenados en el historial de comidas.
     */
    @Query("DELETE FROM diario_comidas")
    suspend fun eliminarTodoElHistorial()
}