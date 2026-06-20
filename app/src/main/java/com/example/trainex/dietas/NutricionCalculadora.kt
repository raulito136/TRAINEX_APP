package com.example.trainex.utils

import com.example.trainex.perfil.Usuario
import kotlin.math.roundToInt

/**
 * DTO para almacenar los resultados del cálculo nutricional.
 */
data class ObjetivosNutricionales(
    val calorias: Int,
    val proteinas: Int,
    val carbohidratos: Int,
    val grasas: Int
)

/**
 * Utilidad para el cálculo de requerimientos energéticos y macronutrientes.
 * Utiliza la ecuación de Mifflin-St Jeor para determinar el Tasa Metabólica Basal (TMB).
 */
object NutricionCalculadora {

    /**
     * Calcula los objetivos diarios basados en el perfil antropométrico y estilo de vida del usuario.
     * @param usuario Datos del perfil (peso, altura, edad, género, actividad, objetivo).
     * @return [ObjetivosNutricionales] con el reparto de calorías y macros.
     */
    fun calcularObjetivos(usuario: Usuario): ObjetivosNutricionales {
        // Validación de datos de seguridad.
        if (usuario.peso <= 0 || usuario.altura <= 0 || usuario.edad <= 0) {
            return ObjetivosNutricionales(0, 0, 0, 0)
        }

        // 1. Cálculo de TMB mediante Mifflin-St Jeor.
        val peso = usuario.peso
        val altura = usuario.altura
        val edad = usuario.edad

        var tmb = (10 * peso) + (6.25 * altura) - (5 * edad)

        // Ajuste por género: +5 para hombres, -161 para mujeres.
        val esHombre = usuario.genero.equals("Hombre", ignoreCase = true) ||
                usuario.genero.equals("Man", ignoreCase = true)

        if (esHombre) {
            tmb += 5
        } else {
            tmb -= 161
        }

        // 2. Aplicación del factor de actividad física (NEAT + EAT).
        val factorActividad = when (usuario.estiloVida) {
            "Sedentario", "Sedentary" -> 1.2
            "Ligero", "Lightly Active" -> 1.375
            "Moderado", "Moderately Active" -> 1.55
            "Activo", "Active" -> 1.725
            "Muy activo", "Very Active" -> 1.9
            else -> 1.2
        }

        val tdee = tmb * factorActividad // Gasto Energético Total Diario.

        // 3. Ajuste según el objetivo del usuario (Déficit, Superávit o Mantenimiento).
        val caloriasFinales = when (usuario.objetivo) {
            "Perder Peso", "Perder peso", "Lose weight", "Fat loss" -> tdee - 500.0
            "Ganar Músculo", "Ganar músculo", "Build muscle", "Hypertrophy (Muscle gain)" -> tdee + 300.0
            "Mantener Peso", "Mantener peso", "Maintain weight" -> tdee
            else -> tdee
        }

        // Seguridad nutricional: Nunca bajar de 1200 kcal.
        val caloriasAjustadas = if (caloriasFinales < 1200) 1200.0 else caloriasFinales

        // 4. Reparto de Macronutrientes (Proporción 30/50/20).
        // Proteínas: 30% (4 kcal/g), Carbohidratos: 50% (4 kcal/g), Grasas: 20% (9 kcal/g).
        val proteinasGramos = (caloriasAjustadas * 0.30) / 4
        val carbohidratosGramos = (caloriasAjustadas * 0.50) / 4
        val grasasGramos = (caloriasAjustadas * 0.20) / 9

        return ObjetivosNutricionales(
            calorias = caloriasAjustadas.roundToInt(),
            proteinas = proteinasGramos.roundToInt(),
            carbohidratos = carbohidratosGramos.roundToInt(),
            grasas = grasasGramos.roundToInt()
        )
    }
}