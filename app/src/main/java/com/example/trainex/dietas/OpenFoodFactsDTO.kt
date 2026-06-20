package com.example.trainex.dietas

import com.google.gson.annotations.SerializedName

/**
 * Clase raíz que representa la respuesta global de la búsqueda.
 */
data class OFFSearchResponse(
    // Contiene la lista de productos encontrados.
    val products: List<OFFProduct>?
)

/**
 * Representa un producto individual devuelto por la API.
 */
data class OFFProduct(
    // @SerializedName vincula la clave del JSON con el nombre de nuestra variable.
    @SerializedName("product_name") val productName: String?,
    val brands: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("image_front_small_url") val imageSmallUrl: String?,

    // Información nutricional del producto.
    val nutriments: OFFNutriments?,

    // Unidad de medida por porción (ej: g, ml).
    @SerializedName("serving_quantity_unit") val unit: String?,

    // Categorías a las que pertenece el producto.
    @SerializedName("categories") val categories: String?
)

/**
 * Contiene los valores nutricionales específicos por cada 100g.
 */
data class OFFNutriments(
    @SerializedName("energy-kcal_100g") val kcal100g: Double?,
    @SerializedName("proteins_100g") val proteins100g: Double?,
    @SerializedName("carbohydrates_100g") val carbs100g: Double?,
    @SerializedName("fat_100g") val fat100g: Double?
)