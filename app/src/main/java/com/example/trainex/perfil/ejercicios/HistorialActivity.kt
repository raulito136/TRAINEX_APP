package com.example.trainex.historial

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trainex.R
import com.example.trainex.databinding.ActivityHistorialBinding
import com.example.trainex.perfil.ejercicios.DetalleHistorialActivity
import com.example.trainex.utils.LanguageUtils
import kotlinx.coroutines.launch

/**
 * Actividad principal que muestra la lista de todos los ejercicios realizados.
 * Permite buscar ejercicios por nombre y navegar al detalle estadístico de cada uno.
 */
class HistorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialBinding
    private lateinit var adapter: HistorialAdapter
    private val viewModel: HistorialViewModel by viewModels()
    private var userIdExterno: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHistorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** Recupera el ID si se está visualizando el historial de otro usuario */
        userIdExterno = intent.getStringExtra("USER_ID")

        setupRecyclerView()
        setupBuscador()
        configurarSearchView()
        observarViewModel()

        /** Carga inicial del historial */
        viewModel.cargarHistorial(userIdExterno)

        binding.tvBack.setOnClickListener { finish() }
    }

    /** Maneja la visibilidad de estados (Cargando, Vacío, Éxito) y actualiza la lista del adaptador */
    private fun observarViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is HistorialViewModel.HistorialUiState.Loading -> {
                        binding.tvEmptyState.text = "Cargando..."
                        binding.tvEmptyState.visibility = View.VISIBLE
                        binding.rvHistorial.visibility = View.GONE
                    }
                    is HistorialViewModel.HistorialUiState.Success -> {
                        binding.tvEmptyState.visibility = View.GONE
                        binding.rvHistorial.visibility = View.VISIBLE
                        adapter.actualizarLista(state.ejercicios)
                    }
                    is HistorialViewModel.HistorialUiState.Empty -> {
                        binding.tvEmptyState.text = "No se han realizado ejercicios"
                        binding.tvEmptyState.visibility = View.VISIBLE
                    }
                    is HistorialViewModel.HistorialUiState.NoResults -> {
                        binding.tvEmptyState.text = "No se encontraron ejercicios"
                        binding.tvEmptyState.visibility = View.VISIBLE
                        binding.rvHistorial.visibility = View.GONE
                    }
                    is HistorialViewModel.HistorialUiState.Error -> {
                        binding.tvEmptyState.text = state.message
                        binding.tvEmptyState.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    /** Configura la lista (RecyclerView) y define la navegación al detalle del ejercicio seleccionado */
    private fun setupRecyclerView() {
        adapter = HistorialAdapter(emptyList()) { ejercicio ->
            val intent = Intent(this, DetalleHistorialActivity::class.java).apply {
                putExtra("EJERCICIO", ejercicio)
                if (!userIdExterno.isNullOrEmpty()) putExtra("USER_ID", userIdExterno)
            }
            startActivity(intent)
        }
        binding.rvHistorial.layoutManager = LinearLayoutManager(this)
        binding.rvHistorial.adapter = adapter
    }

    /** Implementa la lógica de filtrado reactivo mientras el usuario escribe en el buscador */
    private fun setupBuscador() {
        binding.svHistorial.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                binding.svHistorial.clearFocus()
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filtrarLista(newText)
                return true
            }
        })
    }

    /** Ajusta estéticamente los colores del texto y el "hint" dentro del componente SearchView */
    private fun configurarSearchView() {
        val searchTextId = binding.svHistorial.context.resources.getIdentifier("android:id/search_src_text", null, null)
        val searchText = binding.svHistorial.findViewById<TextView>(searchTextId)
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.primary, typedValue, true)
        val color = if (typedValue.resourceId != 0) ContextCompat.getColor(this, typedValue.resourceId) else typedValue.data
        searchText?.setTextColor(color)
        searchText?.setHintTextColor(color)
    }
}