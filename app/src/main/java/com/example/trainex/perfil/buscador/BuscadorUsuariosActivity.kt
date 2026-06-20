package com.example.trainex.perfil.buscador

import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trainex.databinding.ActivityBuscadorUsuariosBinding
import com.example.trainex.utils.LanguageUtils
import kotlinx.coroutines.launch

/** Actividad principal para la búsqueda de otros usuarios en la plataforma */
class BuscadorUsuariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuscadorUsuariosBinding
    private lateinit var adapter: BuscadorAdapter
    private val viewModel: BuscadorUsuariosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        /** Aplica el idioma configurado antes de crear la vista */
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityBuscadorUsuariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarInsets()
        setupRecyclerView()
        setupBuscador()
        observarViewModel()

        /** Carga inicial de los usuarios que el usuario actual ya sigue */
        viewModel.cargarSeguidos()
    }

    /** Ajusta el padding de la vista para respetar las barras del sistema (edge-to-edge) */
    private fun configurarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /** Recolecta los cambios de la lista de usuarios desde el StateFlow del ViewModel */
    private fun observarViewModel() {
        lifecycleScope.launch {
            viewModel.usuarios.collect { lista ->
                adapter.actualizarLista(lista)
            }
        }
    }

    /** Configura el RecyclerView y define la navegación hacia el perfil público al pulsar un usuario */
    private fun setupRecyclerView() {
        adapter = BuscadorAdapter(emptyList()) { usuario ->
            val intent = Intent(this, PerfilPublicoActivity::class.java)
            intent.putExtra("USER_ID", usuario.id)
            startActivity(intent)
        }
        binding.rvResultadosUsuarios.layoutManager = LinearLayoutManager(this)
        binding.rvResultadosUsuarios.adapter = adapter
    }

    /** Configura los listeners del componente SearchView para búsquedas en tiempo real */
    private fun setupBuscador() {
        binding.svUsuarios.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.svUsuarios.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.buscarUsuarios(newText ?: "")
                return true
            }
        })
    }
}