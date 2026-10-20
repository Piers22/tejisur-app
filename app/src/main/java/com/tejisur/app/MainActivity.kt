package com.tejisur.app

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.tejisur.app.databinding.ActivityMainBinding
import com.tejisur.app.produccion.model.AlertaMerma
import com.tejisur.app.produccion.ui.ConsultaMermasFragment
import com.tejisur.app.produccion.ui.ProduccionViewModel
import com.tejisur.app.produccion.ui.RegistroConsumoFragment
import com.tejisur.app.produccion.ui.RegistroMermaFragment
import com.tejisur.app.produccion.ui.LoginFragment

/**
 * Contenedor de las secciones de Producción con una barra inferior:
 * Consumo (HU01), Merma (HU02) y Consulta (HU03 / HU16).
 *
 * Los fragments comparten el mismo [ProduccionViewModel] (activityViewModels),
 * por eso lo registrado en una sección aparece de inmediato en la consulta.
 * La Activity observa la alerta de HU16 para mostrarla sin importar la
 * sección en la que se encuentre el usuario.
 */
class MainActivity : AppCompatActivity() {

    private enum class Seccion { CONSUMO, MERMA, CONSULTA }

    private lateinit var binding: ActivityMainBinding

    private val viewModel: ProduccionViewModel by viewModels()

    private var seccionActual = Seccion.CONSUMO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarMargenesDeSistema()

        seccionActual = savedInstanceState?.getString(KEY_SECCION)
            ?.let { Seccion.valueOf(it) }
            ?: Seccion.CONSUMO

        if (savedInstanceState == null) {
            mostrarSeccion(seccionActual, forzar = true)
        } else {
            marcarNavegacion() // El FragmentManager ya restauró el fragment visible
        }

        binding.navConsumo.setOnClickListener { mostrarSeccion(Seccion.CONSUMO) }
        binding.navMerma.setOnClickListener { mostrarSeccion(Seccion.MERMA) }
        binding.navConsulta.setOnClickListener { mostrarSeccion(Seccion.CONSULTA) }

        // HU16 - Alerta por merma excesiva
        viewModel.alertaMerma.observe(this) { evento ->
            evento.obtenerSiNoManejado()?.let(::mostrarAlertaMerma)
        }
    }

    private fun mostrarSeccion(seccion: Seccion, forzar: Boolean = false) {
        if (seccion == seccionActual && !forzar) return
        seccionActual = seccion
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.contenedorFragment, crearFragment(seccion))
        }
        marcarNavegacion()
    }

    private fun crearFragment(seccion: Seccion): Fragment = when (seccion) {
        Seccion.CONSUMO -> RegistroConsumoFragment()
        Seccion.MERMA -> RegistroMermaFragment()
        Seccion.CONSULTA -> ConsultaMermasFragment()
    }

    /** Resalta la pestaña activa con el color primario de TEJISUR. */
    private fun marcarNavegacion() {
        val pestanas: Map<Seccion, TextView> = mapOf(
            Seccion.CONSUMO to binding.navConsumo,
            Seccion.MERMA to binding.navMerma,
            Seccion.CONSULTA to binding.navConsulta
        )
        pestanas.forEach { (seccion, vista) ->
            val activa = seccion == seccionActual
            val color = ContextCompat.getColor(
                this, if (activa) R.color.tejisur_primary else R.color.tejisur_text_secondary
            )
            vista.isSelected = activa
            vista.setTextColor(color)
            TextViewCompat.setCompoundDrawableTintList(vista, ColorStateList.valueOf(color))
        }
    }
            }
            .setNegativeButton(R.string.alerta_btn_entendido, null)
            .show()
    }

    /** targetSdk 35 dibuja de borde a borde: evita que la barra quede bajo la del sistema. */
    private fun aplicarMargenesDeSistema() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { vista, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SECCION, seccionActual.name)
    }

    private companion object {
        const val KEY_SECCION = "seccion_actual"
    }
}
