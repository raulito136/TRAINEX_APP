package com.example.trainex.perfil.ajustes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trainex.R
import com.example.trainex.databinding.ActivityCuentaBinding
import com.example.trainex.utils.LanguageUtils

/**
 * Pantalla que permite al usuario gestionar los aspectos fundamentales de su cuenta.
 */
class CuentaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCuentaBinding
    private val viewModel: CuentaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Asegura que la aplicación use el idioma configurado
        LanguageUtils.aplicarIdioma(this)
        super.onCreate(savedInstanceState)
        binding = ActivityCuentaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSystemBars()
        setupClickListeners()
        setupObservers()
    }

    /**
     * Suscripción a los eventos de eliminación de cuenta del ViewModel.
     */
    private fun setupObservers() {
        // Observa si la cuenta se eliminó con éxito para cerrar la aplicación por completo
        viewModel.eliminacionExitosa.observe(this) { exito ->
            if (exito) {
                Toast.makeText(this, getString(R.string.mensaje_cuenta_eliminada), Toast.LENGTH_SHORT).show()
                // Cierra todas las actividades y finaliza la app
                finishAffinity()
            }
        }

        // Manejo de errores visuales durante la eliminación
        viewModel.error.observe(this) { msg ->
            msg?.let { Toast.makeText(this, getString(R.string.error_eliminar_cuenta, it), Toast.LENGTH_SHORT).show() }
        }
    }

    /**
     * Configuración de la navegación y acciones de los botones.
     */
    private fun setupClickListeners() {
        binding.tvBack.setOnClickListener { finish() }

        // Navegación a las diferentes pantallas de edición de credenciales
        binding.lytCambiarNombreUsuario.setOnClickListener {
            startActivity(Intent(this, CambiarNombreUsuarioActivity::class.java))
        }

        binding.lytCambiarEmail.setOnClickListener {
            startActivity(Intent(this, CambiarEmailActivity::class.java))
        }

        binding.lytCambiarContrasena.setOnClickListener {
            startActivity(Intent(this, CambiarContrasenaActivity::class.java))
        }

        // Acción para eliminar la cuenta
        binding.btnEliminarCuenta.setOnClickListener {
            mostrarDialogoEliminarCuenta()
        }
    }

    /**
     * Diálogo de seguridad para confirmar una acción irreversible como es borrar la cuenta.
     */
    private fun mostrarDialogoEliminarCuenta() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialogo_titulo_eliminar_cuenta))
            .setMessage(getString(R.string.dialogo_mensaje_eliminar_cuenta))
            .setPositiveButton(getString(R.string.accion_confirmar_ajustes_cuenta)) { _, _ ->
                viewModel.eliminarCuenta()
            }
            .setNegativeButton(getString(R.string.accion_cancelar_ajustes_cuenta), null)
            .show()
    }

    /**
     * Configura el diseño edge-to-edge respetando las barras del sistema.
     */
    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}