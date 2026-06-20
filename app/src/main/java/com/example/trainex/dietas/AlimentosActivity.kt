package com.example.trainex.dietas

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trainex.R
import com.example.trainex.databinding.ActivityAlimentosBinding
import com.example.trainex.repository.FirebaseAlimentosRepository
import com.example.trainex.utils.LanguageUtils
import com.google.android.material.tabs.TabLayout

/**
 * Actividad principal del módulo de dietas que gestiona la búsqueda y visualización de alimentos.
 * Permite alternar entre un catálogo global (API/Firebase) y los alimentos personalizados del usuario.
 */
class AlimentosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlimentosBinding
    private lateinit var adapter: AlimentosAdapter

    /**
     * ViewModel encargado de la lógica de búsqueda y filtrado de alimentos.
     * Se utiliza un Factory para inyectar el repositorio de Firebase.
     */
    private val viewModel: AlimentosViewModel by viewModels {
        AlimentosViewModelFactory(FirebaseAlimentosRepository(this))
    }

    private var tipoComida: String = "Desayuno"

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityAlimentosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recupera el tipo de comida (Desayuno, Almuerzo, etc.) enviado desde la pantalla anterior
        tipoComida = intent.getStringExtra("TIPO_COMIDA") ?: "Desayuno"

        configurarUI()
        configurarSearchView()
        setupObservers()

        // Carga inicial de datos basada en el idioma actual del sistema
        val idioma = LanguageUtils.getIdiomaActual(this)
        viewModel.iniciarBusqueda("", 0, idioma)
    }

    /**
     * Configura los observadores de LiveData para reaccionar a cambios en el estado del ViewModel.
     */
    private fun setupObservers() {
        // Observa cambios en la lista de alimentos para actualizar el RecyclerView
        viewModel.listaAlimentos.observe(this) { lista ->
            adapter.actualizarLista(lista)
        }

        // Gestiona la visibilidad del indicador de carga (ProgressBar)
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

    }

    /**
     * Inicializa los componentes de la interfaz de usuario, adaptadores y listeners.
     */
    private fun configurarUI() {
        // Ajuste de márgenes para diseño Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configuración del adaptador con callbacks para clic normal y clic largo
        adapter = AlimentosAdapter(
            listaAlimentos = emptyList(),
            onAlimentoClick = { alimento ->
                // Muestra el detalle del alimento en un BottomSheet para registrarlo en el diario
                DetalleAlimentoBottomSheet.newInstance(alimento, tipoComida)
                    .show(supportFragmentManager, DetalleAlimentoBottomSheet.TAG)
            },
            onAlimentoLongClick = { alimento ->
                // Permite borrar alimentos solo si se encuentra en la pestaña de "Mis Alimentos"
                if (binding.tabLayout.selectedTabPosition == 1) {
                    mostrarDialogoBorrar(alimento)
                }
            }
        )

        binding.rvAlimentos.layoutManager = LinearLayoutManager(this)
        binding.rvAlimentos.adapter = adapter

        binding.btnCancelar.setOnClickListener { finish() }

        binding.btnNuevoAlimento.setOnClickListener {
            startActivity(Intent(this, CrearAlimentoActivity::class.java))
        }

        // Listener para cambiar entre el catálogo global y los alimentos personales
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.svBusqueda.setQuery("", false)
                val idioma = LanguageUtils.getIdiomaActual(this@AlimentosActivity)
                if (tab?.position == 1) viewModel.cargarMisAlimentos(idioma)
                else viewModel.iniciarBusqueda("", 0, idioma)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Listener para procesar la búsqueda de texto en tiempo real
        binding.svBusqueda.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.iniciarBusqueda(query, binding.tabLayout.selectedTabPosition, LanguageUtils.getIdiomaActual(this@AlimentosActivity))
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.iniciarBusqueda(newText, binding.tabLayout.selectedTabPosition, LanguageUtils.getIdiomaActual(this@AlimentosActivity))
                return true
            }
        })
    }

    /**
     * Ajusta los colores del texto y el "hint" del SearchView para que coincidan con el tema de la app.
     */
    private fun configurarSearchView() {
        val searchTextId = binding.svBusqueda.context.resources.getIdentifier("android:id/search_src_text", null, null)
        val searchText = binding.svBusqueda.findViewById<TextView>(searchTextId)

        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.primary, typedValue, true)

        val color = if (typedValue.resourceId != 0) {
            ContextCompat.getColor(this, typedValue.resourceId)
        } else {
            typedValue.data
        }

        searchText?.setTextColor(color)
        searchText?.setHintTextColor(color)
    }

    /**
     * Muestra un diálogo de confirmación antes de eliminar un alimento del catálogo personal.
     * @param alimento El objeto [Alimento] que se desea eliminar.
     */
    private fun mostrarDialogoBorrar(alimento: Alimento) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_eliminar_titulo))
            .setMessage(getString(R.string.dialog_eliminar_mensaje, alimento.nombre))
            .setPositiveButton(getString(R.string.btn_eliminar)) { _, _ ->
                viewModel.eliminarAlimento(alimento)
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }
}