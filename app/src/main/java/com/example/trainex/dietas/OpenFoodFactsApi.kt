import com.example.trainex.dietas.OFFSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interfaz de Retrofit para definir los puntos de acceso (endpoints)
 * de la API de Open Food Facts.
 */
interface OpenFoodFactsApi {

    // Define una petición GET al script de búsqueda de la API.
    // La acción es 'process' y solicitamos el formato JSON.
    @GET("cgi/search.pl?action=process&json=true")
    suspend fun buscarAlimentos(
        // El término de búsqueda que introduce el usuario.
        @Query("search_terms") query: String,

        // El código de idioma para los resultados (por defecto español).
        @Query("lc") language: String = "es",

        // El código de país para filtrar productos (por defecto España).
        @Query("cc") country: String = "es",

        // Filtramos solo los campos necesarios para no saturar el ancho de banda.
        @Query("fields") fields: String = "product_name,brands,image_url,image_front_small_url,nutriments,serving_quantity_unit,categories",

        // El número máximo de productos que devolverá la búsqueda.
        @Query("page_size") limit: Int = 20
    ): retrofit2.Response<OFFSearchResponse>
}