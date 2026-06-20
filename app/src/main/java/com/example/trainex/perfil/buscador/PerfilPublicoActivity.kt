package com.example.trainex.perfil.buscador

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.trainex.R
import com.example.trainex.databinding.ActivityPerfilPublicoBinding
import com.example.trainex.historial.HistorialActivity
import com.example.trainex.perfil.ChartsAdapter
import com.example.trainex.perfil.calendario.HistorialCalendarioActivity
import com.example.trainex.perfil.foto.FotoProgreso
import com.example.trainex.perfil.foto.FotosAdapter
import com.example.trainex.perfil.medidas.MedidasActivity
import com.example.trainex.perfil.seguidos_seguidores.SeguidosSeguidoresActivity
import com.example.trainex.utils.LanguageUtils
import com.example.trainex.utils.UnitManager // Importación añadida para la conversión
import com.github.chrisbanes.photoview.PhotoView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

/** Pantalla que muestra el perfil de otro usuario, sus estadísticas y progreso */
class PerfilPublicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilPublicoBinding
    private val viewModel: PerfilPublicoViewModel by viewModels()
    private lateinit var adapterFotos: FotosAdapter
    private var userIdObjetivo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilPublicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** Valida que se haya recibido un ID de usuario válido */
        userIdObjetivo = intent.getStringExtra("USER_ID") ?: ""
        if (userIdObjetivo.isEmpty()) { finish(); return }

        setupRecyclerViewFotos()
        initListeners()
        observarViewModel()

        viewModel.cargarDatos(userIdObjetivo)
    }

    /** Observa el estado del UI para actualizar datos de usuario, gráficas y contadores */
    private fun observarViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                state.usuario?.let { pintarUsuario(it) }
                actualizarUIBotonSeguir(state.isFollowing)
                binding.tvSeguidores.text = "${state.seguidoresCount} Seguidores"
                binding.tvSeguidos.text = "${state.seguidosCount} Seguidos"

                adapterFotos.actualizarLista(state.fotos)
                binding.rvFotosProgreso.visibility = if (state.fotos.isEmpty()) View.GONE else View.VISIBLE

                configurarViewPagerGraficas(state.datosVolumen, state.datosPeso)
            }
        }
    }

    /** Muestra los datos físicos del usuario y calcula el IMC en tiempo real */
    private fun pintarUsuario(user: com.example.trainex.perfil.Usuario) {
        // Cálculo del IMC siempre con el sistema métrico para asegurar la exactitud de la fórmula
        val alturaM = if (user.altura > 3) user.altura / 100 else user.altura
        val imc = if (alturaM > 0) user.peso / (alturaM * alturaM) else 0.0

        // Obtener las preferencias de unidad para peso y longitud
        val unidadPeso = UnitManager.obtener(this, "unidad_peso", "kg")
        val pesoVisual = UnitManager.aVisual(user.peso, unidadPeso)

        val unidadLongitud = UnitManager.obtener(this, "unidad_longitud", "cm")
        // Pasamos la altura a cm (alturaM * 100) porque UnitManager espera centímetros
        val alturaVisual = UnitManager.aVisual(alturaM * 100, unidadLongitud)

        binding.tvUserName.text = user.username

        // --- TEXTOS CON STRINGS MULTIIDIOMA Y UNIDADES DINÁMICAS ---

        // Altura con la etiqueta y conversión igual que en PerfilActivity
        val etiquetaAltura = getString(R.string.altura)
        binding.tvHeight.text = "$etiquetaAltura: $alturaVisual $unidadLongitud"

        // Peso usando la etiqueta multiidioma concatenada con el UnitManager
        val etiquetaPeso = getString(R.string.grafico_etiqueta_peso)
        binding.tvWeight.text = "$etiquetaPeso: $pesoVisual $unidadPeso"

        // Edad e IMC usando sus respectivos formats
        binding.tvAge.text = getString(R.string.age_format, user.edad)
        binding.tvBMI.text = getString(R.string.formato_imc, imc)

        // Cargar foto de perfil
        cargarFotoPerfil(user.foto)
    }

    /**
     * Carga la foto de perfil del usuario en ivUserAvatar usando Glide.
     * Soporta URLs (Firebase Storage) y Base64.
     */
    private fun cargarFotoPerfil(foto: String?) {
        if (foto.isNullOrEmpty()) {
            binding.ivUserAvatar.setImageResource(R.drawable.imagen_default_persona)
            return
        }

        if (foto.startsWith("http")) {
            // URL de Firebase Storage o similar
            Glide.with(this)
                .load(foto)
                .placeholder(R.drawable.imagen_default_persona)
                .error(R.drawable.imagen_default_persona)
                .circleCrop()
                .into(binding.ivUserAvatar)
        } else {
            // Imagen en Base64
            try {
                val imageBytes = Base64.decode(foto, Base64.DEFAULT)
                Glide.with(this)
                    .load(imageBytes)
                    .placeholder(R.drawable.imagen_default_persona)
                    .error(R.drawable.imagen_default_persona)
                    .circleCrop()
                    .into(binding.ivUserAvatar)
            } catch (e: Exception) {
                binding.ivUserAvatar.setImageResource(R.drawable.imagen_default_persona)
            }
        }
    }

    /** Cambia el aspecto del botón de seguir dependiendo del estado de seguimiento */
    private fun actualizarUIBotonSeguir(isFollowing: Boolean) {
        binding.btnSeguir.text = if (isFollowing) "Siguiendo" else "Seguir"
        val color = if (isFollowing) android.R.color.darker_gray else R.color.link
        binding.btnSeguir.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, color))
    }

    /** Inicializa los eventos de clic de los botones y textos interactivos */
    private fun initListeners() {
        binding.btnSeguir.setOnClickListener { viewModel.toggleSeguimiento(userIdObjetivo) }
        binding.tvBack.setOnClickListener { finish() }
        binding.btnEjerciciosPublico.setOnClickListener { navegar(HistorialActivity::class.java) }
        binding.btnCalendarioPublico.setOnClickListener { navegar(HistorialCalendarioActivity::class.java) }
        binding.btnMedidasPublico.setOnClickListener { navegar(MedidasActivity::class.java) }
        binding.tvSeguidores.setOnClickListener { abrirListaSocial(0) }
        binding.tvSeguidos.setOnClickListener { abrirListaSocial(1) }
    }

    /** Navega a otras actividades compartiendo el ID del usuario visualizado */
    private fun navegar(cls: Class<*>) {
        val intent = Intent(this, cls).apply { putExtra("USER_ID", userIdObjetivo) }
        startActivity(intent)
    }

    /** Abre la pantalla de seguidores/seguidos en la pestaña indicada */
    private fun abrirListaSocial(tabIndex: Int) {
        val intent = Intent(this, SeguidosSeguidoresActivity::class.java).apply {
            putExtra("USER_ID", userIdObjetivo)
            putExtra("START_TAB", tabIndex)
        }
        startActivity(intent)
    }

    /** Configura el carrusel horizontal de fotos de progreso */
    private fun setupRecyclerViewFotos() {
        adapterFotos = FotosAdapter(mutableListOf(), { foto -> mostrarFotoConZoom(foto) }, {})
        binding.rvFotosProgreso.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFotosProgreso.adapter = adapterFotos
    }

    /** Configura el adaptador para las gráficas de volumen y peso con sus respectivas etiquetas */
    private fun configurarViewPagerGraficas(vol: Pair<LineDataSet, List<String>>?, peso: Pair<LineDataSet, List<String>>?) {
        val sets = mutableListOf<Pair<String, LineDataSet>>()
        val labels = mutableListOf<List<String>>()

        sets.add((vol?.first?.label ?: "Volumen") to (vol?.first ?: LineDataSet(listOf(Entry(0f, 0f)), "")))
        labels.add(vol?.second ?: listOf(""))

        sets.add((peso?.first?.label ?: "Peso") to (peso?.first ?: LineDataSet(listOf(Entry(0f, 0f)), "")))
        labels.add(peso?.second ?: listOf(""))

        binding.vpCharts.adapter = ChartsAdapter(sets, labels)
        TabLayoutMediator(binding.tabLayoutCharts, binding.vpCharts) { tab, pos ->
            tab.text = if (pos == 0) "Volumen" else "Peso"
        }.attach()
    }

    /** Muestra un diálogo de pantalla completa con soporte para zoom sobre la imagen seleccionada */
    private fun mostrarFotoConZoom(foto: FotoProgreso) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val photoView = PhotoView(this)
        dialog.setContentView(photoView)
        val data = if (foto.url.startsWith("http")) foto.url else Base64.decode(foto.url, Base64.DEFAULT)
        Glide.with(this).load(data).into(photoView)
        photoView.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}