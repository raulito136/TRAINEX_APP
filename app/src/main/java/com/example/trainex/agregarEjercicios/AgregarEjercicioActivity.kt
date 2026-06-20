package com.example.trainex.agregarEjercicios

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trainex.R
import com.example.trainex.databinding.ActivityAgregarEjercicioBinding
import com.example.trainex.utils.LanguageUtils
import com.example.trainex.ejercicio.Ejercicio
import kotlinx.coroutines.launch

/**
 * Pantalla principal para la selección de ejercicios.
 * Permite al usuario buscar por texto, filtrar por grupo muscular y seleccionar múltiples ejercicios
 * para devolverlos a la pantalla anterior.
 */
class AgregarEjercicioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAgregarEjercicioBinding
    private val viewModel: AgregarEjercicioViewModel by viewModels()

    private lateinit var adapter: AgregarEjercicioAdapter
    private lateinit var adapterFiltros: FiltroGrupoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAgregarEjercicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val preseleccionados = intent.getSerializableExtra("listaEjercicios") as? ArrayList<Ejercicio>
        preseleccionados?.let { viewModel.setPreselected(it) }

        initRecyclerViews()
        initObservers()
        initListeners()
        configurarSearchView()
    }

    /**
     * Inicializa el RecyclerView principal de ejercicios y su adaptador.
     */
    private fun initRecyclerViews() {
        adapter = AgregarEjercicioAdapter { ejercicioId ->
            viewModel.toggleSelection(ejercicioId)
        }
        binding.rvEjercicios.layoutManager = LinearLayoutManager(this)
        binding.rvEjercicios.adapter = adapter
    }

    /**
     * Configura los observadores de LiveData/Flow para reaccionar a cambios en la lista filtrada,
     * los grupos disponibles y el estado de selección.
     */
    private fun initObservers() {
        // 1. Observar lista filtrada de ejercicios
        lifecycleScope.launch {
            viewModel.filteredExercises.collect { lista ->
                adapter.submitList(lista, viewModel.selectedIds.value)
            }
        }

        // 2. Observar los grupos disponibles para el filtro
        lifecycleScope.launch {
            viewModel.gruposParaFiltro.collect { grupos ->
                if (grupos.size > 1) { // Si hay más que solo "Todos"
                    setupFiltrosGrupos(grupos)
                }
            }
        }

        // 3. Observar selección para el botón
        lifecycleScope.launch {
            viewModel.selectedIds.collect { ids ->
                adapter.updateSelectedIds(ids)
                actualizarTextoBoton(ids.size)
            }
        }
    }

    /**
     * Configura el adaptador horizontal para los filtros de grupos musculares.
     * @param grupos Lista de claves de strings que representan los grupos musculares.
     */
    private fun setupFiltrosGrupos(grupos: List<String>) {
        if (binding.rvFiltrosGrupo.adapter == null) {
            adapterFiltros = FiltroGrupoAdapter(grupos) { grupo ->
                viewModel.updateSelectedGroup(grupo)
            }
            binding.rvFiltrosGrupo.adapter = adapterFiltros
        }
    }

    /**
     * Configura los clics de los botones y el listener de búsqueda del SearchView.
     */
    private fun initListeners() {
        binding.btnAgregarEjercicio.setOnClickListener {
            val intent = Intent().apply {
                putExtra("listaEjercicios", viewModel.getSelectedExercisesList())
            }
            setResult(RESULT_OK, intent)
            finish()
        }

        binding.tvCancelar.setOnClickListener { finish() }

        binding.svBusqueda.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.updateSearchText(newText ?: "")
                return true
            }
        })
    }

    /**
     * Actualiza dinámicamente el texto del botón de confirmación según la cantidad de ejercicios seleccionados.
     * @param cantidad Número de ejercicios actualmente marcados.
     */
    private fun actualizarTextoBoton(cantidad: Int) {
        binding.btnAgregarEjercicio.text = when (cantidad) {
            0 -> getString(R.string.agregar_ejercicio)
            1 -> getString(R.string.add_exercise_singular)
            else -> getString(R.string.add_exercises_plural, cantidad)
        }
    }

    /**
     * Ajusta el color del texto y del hint del SearchView para que coincida con el tema de la aplicación.
     */
    private fun configurarSearchView() {
        val searchTextId = resources.getIdentifier("android:id/search_src_text", null, null)
        val searchText = binding.svBusqueda.findViewById<TextView>(searchTextId)
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.primary, typedValue, true)
        val color = if (typedValue.resourceId != 0) ContextCompat.getColor(this, typedValue.resourceId) else typedValue.data
        searchText.setTextColor(color)
        searchText.setHintTextColor(color)
    }
}