package com.example.trainex.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.trainex.DAO.AlimentoDiarioDAO
import com.example.trainex.DAO.EjercicioDAO
import com.example.trainex.DAO.SerieDAO
import com.example.trainex.dietas.Alimento
import com.example.trainex.dietas.AlimentoDiario
import com.example.trainex.ejercicio.Ejercicio
import com.example.trainex.serie.SerieEntidad
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Configuración principal de la base de datos de Room de la aplicación.
 * Define las entidades, la versión del esquema y provee los métodos de acceso a los DAO.
 */
@Database(entities = [Alimento::class, AlimentoDiario::class, Ejercicio::class, SerieEntidad::class], version = 13, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /** Provee acceso a las operaciones del diario de alimentos. */
    abstract fun diarioDao(): AlimentoDiarioDAO
    /** Provee acceso a las operaciones de la biblioteca de ejercicios. */
    abstract fun ejercicioDao(): EjercicioDAO
    /** Provee acceso a las operaciones del historial de series. */
    abstract fun serieDao(): SerieDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Implementación del patrón Singleton para obtener la instancia única de la base de datos.
         * @param context Contexto de la aplicación para inicializar Room.
         * @return Instancia única de [AppDatabase].
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trainex_global_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Clase interna para gestionar eventos del ciclo de vida de la base de datos.
     */
    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        /**
         * Se ejecuta al crear la base de datos por primera vez para precargar datos iniciales.
         */
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    cargarEjerciciosJson(context, database.ejercicioDao())
                }
            }
        }

        /**
         * Lee un archivo JSON desde los assets de la aplicación y lo inserta en la base de datos.
         * @param context Contexto para acceder a los assets.
         * @param dao DAO de ejercicios para realizar la inserción masiva.
         */
        suspend fun cargarEjerciciosJson(context: Context, dao: EjercicioDAO) {
            try {
                val jsonString = context.assets.open("ejercicios.json")
                    .bufferedReader()
                    .use { it.readText() }

                val listType = object : TypeToken<List<Ejercicio>>() {}.type
                val listaEjercicios: List<Ejercicio> = Gson().fromJson(jsonString, listType)

                dao.insertarTodos(listaEjercicios)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}