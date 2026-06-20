package com.example.trainex.perfil.ajustes

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.trainex.R
import com.example.trainex.databinding.ActivityEditarPerfilBinding
import com.example.trainex.perfil.UserManager
import com.example.trainex.utils.LanguageUtils
import java.io.File

/**
 * Activity encargada de gestionar la edición del perfil del usuario,
 * específicamente la actualización de la foto de perfil mediante cámara o galería.
 */
class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarPerfilBinding
    private val viewModel: EditarPerfilViewModel by viewModels()
    private var uriImagen: Uri? = null // Almacena temporalmente la URI de la foto tomada con la cámara

    // Registro del contrato para seleccionar una imagen de la galería
    private val selecFotoGaleria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        // Si el usuario seleccionó una imagen, se envía al ViewModel para procesarla
        uri?.let { viewModel.subirFoto(it) }
    }

    // Registro del contrato para tomar una foto con la aplicación de cámara
    private val tomarFotoCamara = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        // Si la captura fue exitosa, enviamos la URI temporal al ViewModel
        if (success) {
            uriImagen?.let { viewModel.subirFoto(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicar el idioma configurado antes de inflar la vista
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSystemBars()      // Ajustar paddings para barras de sistema (status bar/nav bar)
        setupClickListeners()  // Configurar eventos de clic
        setupObservers()       // Suscribirse a cambios en el ViewModel
        cargarDatosActuales()  // Mostrar la foto actual guardada localmente
    }

    /**
     * Observa los cambios en el LiveData del ViewModel para actualizar la interfaz.
     */
    private fun setupObservers() {
        viewModel.fotoActualizada.observe(this) { base64 ->
            // Decodificar el String Base64 recibido para mostrarlo con Glide
            val imageBytes = Base64.decode(base64, Base64.DEFAULT)
            Glide.with(this)
                .load(imageBytes)
                .circleCrop() // Recorte circular para la foto de perfil
                .into(binding.ivFotoPerfil)

            Toast.makeText(this, "Foto actualizada correctamente", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Carga la foto actual del usuario desde el UserManager (persistencia local).
     */
    private fun cargarDatosActuales() {
        val perfilLocal = UserManager.getUserProfile(this)
        if (perfilLocal.foto.isNotEmpty()) {
            val imageBytes = Base64.decode(perfilLocal.foto, Base64.DEFAULT)
            Glide.with(this)
                .load(imageBytes)
                .circleCrop()
                .into(binding.ivFotoPerfil)
        }
    }

    /**
     * Define las acciones al pulsar sobre los elementos de la interfaz.
     */
    private fun setupClickListeners() {
        binding.tvBack.setOnClickListener { finish() } // Botón atrás
        binding.tvCambiarFoto.setOnClickListener { mostrarDialogoFoto() }
        binding.ivFotoPerfil.setOnClickListener { mostrarDialogoFoto() }

        // Navegación a la pantalla de datos personales
        binding.lytDatosPersonales.setOnClickListener {
            startActivity(Intent(this, DatosPersonalesActivity::class.java))
        }
    }

    /**
     * Muestra un cuadro de diálogo para que el usuario elija entre Cámara o Galería.
     */
    private fun mostrarDialogoFoto() {
        val opciones = arrayOf(getString(R.string.opcion_hacer_foto), getString(R.string.opcion_galeria))
        AlertDialog.Builder(this)
            .setTitle(R.string.dialogo_titulo_foto)
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> abrirCamara()
                    1 -> selecFotoGaleria.launch("image/*")
                }
            }
            .show()
    }

    /**
     * Prepara un archivo temporal y lanza la cámara para capturar una imagen.
     */
    private fun abrirCamara() {
        try {
            // Crear un archivo temporal en el directorio privado de la app
            val file = File(filesDir, "temp_perfil.jpg")
            // Obtener la URI segura mediante FileProvider configurado en el Manifest
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            uriImagen = uri
            tomarFotoCamara.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al abrir la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Ajusta el padding de la vista principal para que no quede oculta tras las barras de sistema.
     */
    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}