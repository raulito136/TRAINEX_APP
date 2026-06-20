package com.example.trainex.dietas

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.trainex.R
import com.example.trainex.databinding.ActivityCrearAlimentoBinding
import com.example.trainex.utils.LanguageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

/**
 * Actividad encargada de la creación de nuevos alimentos personalizados por el usuario.
 * Gestiona la captura de datos nutricionales, la selección de imágenes y la persistencia en Firebase.
 */
class CrearAlimentoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrearAlimentoBinding
    private val viewModel: CrearAlimentoViewModel by viewModels()

    /** Almacena la ubicación (URI) de la imagen seleccionada o capturada. */
    private var imageUri: Uri? = null

    /**
     * Lanzador para seleccionar una imagen desde la galería del dispositivo.
     * Al recibir la URI, la almacena y actualiza la vista previa en la interfaz.
     */
    private val pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            binding.ivPreviewAlimento.setImageURI(uri)
        }
    }

    /**
     * Lanzador para capturar una foto con la cámara.
     * Convierte el Bitmap recibido en un archivo temporal en el caché para generar una URI compatible con el ViewModel.
     */
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val file = File(cacheDir, "foto_alimento_temp.jpg")
            file.outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            imageUri = Uri.fromFile(file)
            binding.ivPreviewAlimento.setImageBitmap(bitmap)
        }
    }

    private var posicionCategoriaSeleccionada: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityCrearAlimentoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        initListeners()
        setupComboCategorias()
    }

    /**
     * Configura los observadores de LiveData para reaccionar al estado de guardado o errores.
     */
    private fun setupObservers() {
        viewModel.estadoGuardado.observe(this) { exitoso ->
            if (exitoso) {
                Toast.makeText(this, getString(R.string.msg_alimento_guardado), Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.error.observe(this) { msg ->
            binding.btnGuardarAlimento.isEnabled = true
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Recopila la información de los campos de entrada y construye el objeto [Alimento].
     * Valida que el nombre no esté vacío antes de iniciar el proceso de guardado.
     */
    private fun guardarAlimento() {
        val nombre = binding.etNombre.text.toString().trim()
        if (nombre.isEmpty()) {
            binding.etNombre.error = getString(R.string.error_nombre_req)
            return
        }

        binding.btnGuardarAlimento.isEnabled = false

        val alimento = Alimento(
            id = 0,
            firebaseId = FirebaseFirestore.getInstance().collection("alimentos").document().id,
            userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            imagen = "", // El repositorio se encargará de subir la imagen y actualizar este campo
            nombre = nombre,
            marca = binding.etMarca.text.toString(),
            categoria = binding.etCategoria.text.toString(),
            calorias = binding.etCalorias.text.toString().toIntOrNull() ?: 0,
            proteinas = binding.etProteinas.text.toString().toDoubleOrNull() ?: 0.0,
            carbohidratos = binding.etCarbohidratos.text.toString().toDoubleOrNull() ?: 0.0,
            grasas = binding.etGrasas.text.toString().toDoubleOrNull() ?: 0.0,
            esLiquido = binding.toggleUnidadMedida.checkedButtonId == R.id.btnMililitros
        )

        viewModel.guardarAlimento(alimento, imageUri)
    }

    /**
     * Inicializa los listeners de los botones y los cambios de estado en los toggles de la interfaz.
     */
    private fun initListeners() {
        binding.btnCancelarCrear.setOnClickListener { finish() }
        binding.btnGaleria.setOnClickListener { pickMedia.launch("image/*") }
        binding.btnTomarFoto.setOnClickListener { takePicture.launch(null) }
        binding.btnGuardarAlimento.setOnClickListener { guardarAlimento() }

        /**
         * Actualiza los "hints" de los campos nutricionales según la unidad de medida seleccionada (g o ml).
         */
        binding.toggleUnidadMedida.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val sufijo = if (checkedId == R.id.btnGramos) "100g" else "100ml"
                binding.tilCalorias.hint = getString(R.string.hint_calorias_dinamico, sufijo)
                binding.tilGrasas.hint = getString(R.string.hint_grasas_dinamico, sufijo)
                binding.tilCarbohidratos.hint = getString(R.string.hint_carbohidratos_dinamico, sufijo)
                binding.tilProteinas.hint = getString(R.string.hint_proteinas_dinamico, sufijo)
            }
        }

        /**
         * Cambia el sufijo del campo de energía según la unidad seleccionada (kcal o kj).
         */
        binding.toggleEnergia.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnKcal -> binding.tilCalorias.suffixText = "kcal"
                    R.id.btnKj -> binding.tilCalorias.suffixText = "kj"
                }
            }
        }
    }

    /**
     * Configura el adaptador para el desplegable de categorías de alimentos.
     */
    private fun setupComboCategorias() {
        val categoriasDisplay = resources.getStringArray(R.array.categorias_alimentos_array)
        val adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            categoriasDisplay
        )
        binding.etCategoria.setAdapter(adapter)
        binding.etCategoria.setOnItemClickListener { _, _, position, _ ->
            posicionCategoriaSeleccionada = position
        }
    }
}