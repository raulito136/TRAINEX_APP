package com.example.trainex.formulario.registro

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.trainex.R
import com.example.trainex.databinding.ActivityFormulariosRegistroBinding
import com.example.trainex.rutinas.Entrenamiento

/**
 * Activity principal que orquestal el flujo de registro de usuario.
 * Utiliza un sistema de pasos basado en Fragmentos y un ViewModel compartido.
 */
class FormulariosRegistroActivity : AppCompatActivity() {

    // ViewBinding para acceder a los componentes del layout activity_formularios_registro
    private lateinit var binding: ActivityFormulariosRegistroBinding

    // ViewModel de registro con alcance de Activity (compartido con los fragmentos)
    private val viewModel: RegistroViewModel by viewModels()

    // Definición de la secuencia de pantallas del formulario (total: 4 pasos)
    private val fragments: List<Fragment> = listOf(
        UsuarioFragmento(),
        InformacionPersonalFragmento(),
        DatosFisicosFragmento(),
        EstiloVidaFragmento()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflación de la vista mediante ViewBinding
        binding = ActivityFormulariosRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura el valor máximo de la ProgressBar según la cantidad de fragmentos definidos
        binding.progressBar.max = fragments.size

        // Configuración de los observadores de LiveData del ViewModel
        setupObservers()
    }

    /**
     * Suscripción a los cambios de estado en el ViewModel.
     */
    private fun setupObservers() {
        // Observa en qué paso del registro se encuentra el usuario
        viewModel.currentStep.observe(this) { step: Int ->
            // Actualiza la barra de progreso (se suma 1 porque step inicia en 0)
            binding.progressBar.progress = step + 1

            if (step < fragments.size) {
                // Si aún hay fragmentos en la lista, muestra el que corresponde al paso actual
                showFragment(fragments[step])
            } else {
                // Si el step iguala o supera el tamaño de la lista, se procede a guardar en Firebase
                viewModel.finalizarRegistro(getString(R.string.medida_peso_corporal))
            }
        }

        // Observa el resultado final del proceso de registro/guardado
        viewModel.registroExitoso.observe(this) { exitoso: Boolean ->
            if (exitoso) {
                // Si fue exitoso, navega hacia la actividad de Entrenamiento y limpia el stack de actividades
                val intent = Intent(this, Entrenamiento::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    /**
     * Realiza el reemplazo del fragmento en el contenedor principal con una animación de transición.
     */
    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out) // Animación de desvanecimiento
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}