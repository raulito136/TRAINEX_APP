package com.example.trainex.perfil.ajustes

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trainex.PerfilActivity
import com.example.trainex.R
import com.example.trainex.databinding.ActivityAjustesBinding
import com.example.trainex.login.LoginActivity
import com.example.trainex.utils.LanguageUtils
import com.example.trainex.utils.ModoUtils

/**
 * Actividad que presenta el menú de ajustes del perfil.
 * Gestiona la navegación a sub-secciones y la lógica de cierre de sesión.
 */
class AjustesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAjustesBinding
    // ViewModel que gestiona la lógica de cierre de sesión y limpieza de datos
    private val viewModel: AjustesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplica el tema (Oscuro/Claro) e idioma guardados antes de cargar la vista
        ModoUtils.aplicarModoGuardado(this)
        LanguageUtils.aplicarIdioma(this)

        super.onCreate(savedInstanceState)
        binding = ActivityAjustesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración de la interfaz y observadores
        setupSystemBars()
        setupClickListeners()
        setupObservers()
    }

    /**
     * Observa eventos del ViewModel, específicamente el resultado del logout.
     */
    private fun setupObservers() {
        viewModel.logoutEvent.observe(this) { exito ->
            if (exito) {
                // Si el cierre de sesión fue exitoso, redirige al Login y limpia el stack
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
                Toast.makeText(this, getString(R.string.sesion_cerrada_cerrarsesion), Toast.LENGTH_SHORT).show()
            } else {
                // Informa si ocurrió un error durante el proceso de logout
                Toast.makeText(this, getString(R.string.error_cerrar_sesion_cerrarsesion), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Configura los listeners para los botones y secciones de ajustes.
     */
    private fun setupClickListeners() {
        // Botón de retroceso
        binding.tvBack.setOnClickListener { finish() }

        // Gestión del botón de cierre de sesión con diálogo de confirmación
        binding.btnCerrarSesion.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.titulo_cerrarsesion))
                .setMessage(getString(R.string.mensaje_confirmacion_cerrarsesion))
                .setPositiveButton(getString(R.string.boton_si_cerrarsesion)) { _, _ -> viewModel.cerrarSesion() }
                .setNegativeButton(getString(R.string.boton_no_cerrarsesion), null)
                .show()
        }

        // Navegación a las diferentes pantallas de configuración
        binding.lytCuenta.setOnClickListener {
            startActivity(Intent(this, CuentaActivity::class.java))
        }
        binding.lytNotificaciones.setOnClickListener {
            startActivity(Intent(this, NotificacionesActivity::class.java))
        }
        binding.lytUnidades.setOnClickListener {
            startActivity(Intent(this, UnidadesActivity::class.java))
        }
        binding.lytIdioma.setOnClickListener {
            startActivity(Intent(this, IdiomaActivity::class.java))
        }
        binding.lytModo.setOnClickListener {
            startActivity(Intent(this, ModoActivity::class.java))
        }
        binding.lytPerfil.setOnClickListener {
            startActivity(Intent(this, EditarPerfilActivity::class.java))
        }

        // Enlace a redes sociales
        binding.ivInstagram.setOnClickListener { abrirEnlaceWeb("https://instagram.com") }
        binding.ivFacebook.setOnClickListener { abrirEnlaceWeb("https://facebook.com") }
        binding.ivTwitter.setOnClickListener { abrirEnlaceWeb("https://x.com") }
        binding.ivYoutube.setOnClickListener { abrirEnlaceWeb("https://youtube.com") }

    }

    /**
     * Intenta abrir una URL externa en el navegador del dispositivo.
     */
    private fun abrirEnlaceWeb(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(this, "Navegador no disponible", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Ajusta el padding de la vista para respetar las barras del sistema (status bar, navigation bar).
     */
    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}